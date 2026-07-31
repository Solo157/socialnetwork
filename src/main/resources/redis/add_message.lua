local key = KEYS[1]
local timestamp = ARGV[1]
local message = ARGV[2]
local ttl = ARGV[3]

redis.call('ZADD', key, timestamp, message)
redis.call('EXPIRE', key, ttl)
