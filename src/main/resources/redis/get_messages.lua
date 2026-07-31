local key = KEYS[1]

return redis.call('ZRANGEBYSCORE', key, '-inf', '+inf', 'WITHSCORES')
