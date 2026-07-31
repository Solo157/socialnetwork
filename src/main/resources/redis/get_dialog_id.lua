local dialogId = redis.call('GET', KEYS[1])

if dialogId then
    return dialogId
end

dialogId = redis.call('GET', KEYS[2])

if dialogId then
    return dialogId
end

return nil