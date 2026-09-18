package org.counter.handler;

import com.socialnetwork.dialog.grpc.Message;

import java.util.*;

/**
 * Бизнес-логика диалогов. Конкретная реализация выбирается по свойству
 * sqlDB.enabled: SqlDialogService (PostgreSQL + outbox) или RedisDialogService.
 */
public interface DialogServiceHandler {

    /**
     * Сохраняет сообщение в диалоге между senderId и receiverId,
     * создавая диалог при необходимости.
     */
    void sendMessage(String senderId, String receiverId, String text);

    /**
     * Возвращает сообщения диалога между senderId и receiverId.
     * Просматривает диалог пользователь senderId, поэтому в SQL-реализации
     * его непрочитанные сообщения при этом помечаются прочитанными.
     */
    List<Message> listMessages(String senderId, String receiverId);

}
