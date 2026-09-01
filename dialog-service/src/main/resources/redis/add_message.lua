local key = KEYS[1]

local message = ARGV[1]

local ttl = ARGV[2]

redis.call('RPUSH', key, message)
redis.call('EXPIRE', key, ttl)
