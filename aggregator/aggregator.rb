require 'twitter'
require 'kafka'

topic = ENV['KAFKA_TOPIC']

client = Twitter::Streaming::Client.new do |config|
  config.consumer_key         = ENV['TWITTER_CONSUMER_KEY']
  config.consumer_secret      = ENV['TWITTER_CONSUMER_SECRET']
  config.access_token         = ENV['TWITTER_ACCESS_TOKEN']
  config.access_token_secret  = ENV['TWITTER_ACCESS_TOKEN_SECRET']
end

rest_client = Twitter::REST::Client.new do |config|
  config.consumer_key         = ENV['TWITTER_CONSUMER_KEY']
  config.consumer_secret      = ENV['TWITTER_CONSUMER_SECRET']
  config.access_token         = ENV['TWITTER_ACCESS_TOKEN']
  config.access_token_secret  = ENV['TWITTER_ACCESS_TOKEN_SECRET']
end

# WOEID for New York
trends = rest_client.trends(2_459_115).map(&:name)

kafka = Kafka.new(ENV['KAFKA_BROKERS'].split(','), client_id: ENV['KAFKA_CLIENT_ID'])
client.filter(track: trends.join(',')) do |object|
  if object.is_a? Twitter::Tweet and object.text.ascii_only? and not object.retweet?
    kafka.deliver_message(object.text, topic: topic)
    puts object.text
  end
end