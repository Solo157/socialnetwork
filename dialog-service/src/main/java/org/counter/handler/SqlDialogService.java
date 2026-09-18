package org.counter.handler;

import com.socialnetwork.dialog.grpc.Message;
import lombok.RequiredArgsConstructor;
import org.counter.annotation.DialogSqlEnabled;
import org.counter.model.CounterEvent;
import org.counter.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * SQL-реализация сервиса диалогов (активна при sqlDB.enabled=true).
 *
 * Паттерн transactional outbox: вместе с основным действием (создание
 * сообщения или пометка прочтения) в той же транзакции пишется запись в
 * таблицу message_outbox. {@code Scheduler} периодически отправляет записи
 * со статусом NEW в Kafka и помечает их SENT, поэтому события не теряются,
 * даже если сервис упадёт между записью в БД и отправкой в Kafka.
 */
@Service
@RequiredArgsConstructor
@DialogSqlEnabled
public class SqlDialogService implements DialogServiceHandler {

    private final DialogRepository dialogRepository;
    private final DialogMessageRepository dialogMessageRepository;
    private final DialogMessageOutboxRepository outboxRepository;

    /**
     * Отправка сообщения: сохраняем сообщение и в той же транзакции
     * записываем в outbox событие message.created для получателя.
     * Событие создаётся только для receiverId — отправителю счётчик
     * непрочитанных не нужен.
     */
    @Transactional
    @Override
    public void sendMessage(String senderId, String receiverId, String text) {
        // Диалог ищем по паре участников в любом порядке
        String dialogId;
        Optional<DialogEntity> dialogOpt = dialogRepository.findByParticipants(senderId, receiverId);
        if (dialogOpt.isEmpty()) {
            dialogId = UUID.randomUUID().toString();
            dialogRepository.save(new DialogEntity(dialogId, senderId, receiverId, LocalDateTime.now()));
        } else {
            dialogId = dialogOpt.get().getId();
        }

        DialogMessageEntity message = DialogMessageEntity.builder()
                .id(UUID.randomUUID().toString())
                .dialogId(dialogId)
                .senderId(senderId)
                .receiverId(receiverId)
                .text(text)
                .createdAt(LocalDateTime.now())
                .build();

        dialogMessageRepository.save(message);

        // Событие «сообщение появилось» — только для получателя
        outboxRepository.insert(buildOutbox(message.getId(), receiverId, dialogId, senderId,
                CounterEvent.EVENT_MESSAGE_CREATED, message.getCreatedAt()));
    }

    /**
     * Помечает сообщения прочитанными для readerId и пишет в outbox события
     * message.read, чтобы counter-service уменьшил счётчик непрочитанных.
     *
     * Учитываются только сообщения, адресованные readerId и ещё не прочитанные
     * (read_at IS NULL), поэтому повторный вызов новых событий не создаёт.
     */
    @Transactional
    public void markMessagesRead(String readerId, List<String> messageIds) {
        List<DialogMessageEntity> unread = findUnreadByIds(readerId, messageIds);
        if (unread.isEmpty()) {
            return;
        }

        List<String> ids = new ArrayList<>();
        for (DialogMessageEntity message : unread) {
            ids.add(message.getId());
            outboxRepository.insert(buildOutbox(message.getId(), readerId, message.getDialogId(),
                    message.getSenderId(), CounterEvent.EVENT_MESSAGE_READ, LocalDateTime.now()));
        }

        dialogMessageRepository.markRead(ids, readerId);
    }

    @Transactional(readOnly = true)
    public List<DialogMessageEntity> findUnreadByIds(String readerId, List<String> messageIds) {
        return dialogMessageRepository.findUnreadByIds(readerId, messageIds);
    }

    /**
     * Возвращает сообщения диалога между senderId и receiverId.
     *
     * Побочный эффект: запрос делает пользователь senderId, то есть именно он
     * просматривает диалог, — поэтому его непрочитанные сообщения в этом
     * диалоге помечаются прочитанными (read_at) и в outbox попадают события
     * message.read (NEW), которые паблишер доставит в counter-service.
     */
    @Transactional
    @Override
    public List<Message> listMessages(String senderId, String receiverId) {
        List<DialogMessageEntity> messages = dialogMessageRepository.findBySenderIdAndReceiver(senderId, receiverId);

        markMessagesRead(senderId, messages.stream().map(DialogMessageEntity::getId).toList());

        return messages.stream()
                .map(this::toProto)
                .toList();
    }

    private DialogMessageOutbox buildOutbox(String messageId, String receiverId, String dialogId,
                                            String senderId, String eventType, LocalDateTime createdAt) {
        return DialogMessageOutbox.builder()
                .id(UUID.randomUUID().toString())
                .messageId(messageId)
                .receiverId(receiverId)
                .dialogId(dialogId)
                .senderId(senderId)
                .eventType(eventType)
                .status(DialogMessageOutbox.STATUS_NEW)
                .createdAt(createdAt)
                .build();
    }

    private Message toProto(DialogMessageEntity message) {
        return Message.newBuilder()
                .setId(message.getId())
                .setDialogId(message.getDialogId())
                .setSenderId(message.getSenderId())
                .setReceiverId(message.getReceiverId())
                .setText(message.getText())
                .setCreatedAt(message.getCreatedAt().toString())
                .build();
    }

}
