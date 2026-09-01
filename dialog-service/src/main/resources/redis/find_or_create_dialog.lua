local dialogId = redis.call('GET', KEYS[1])

if dialogId then
    return dialogId
end

dialogId = redis.call('GET', KEYS[2])

if dialogId then
    return dialogId
end

dialogId = ARGV[1]

redis.call('SET', KEYS[1], dialogId, 'EX', ARGV[2])
redis.call('SET', KEYS[2], dialogId, 'EX', ARGV[2])

return dialogId