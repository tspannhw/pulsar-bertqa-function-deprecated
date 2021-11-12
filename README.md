## pulsar-djl-example

## setup

````

bin/pulsar-admin topics create persistent://public/default/chat

bin/pulsar-admin topics create persistent://public/default/chatresult


bin/pulsar-admin functions create --auto-ack true --jar pulsardjlexample-1.0.jar --classname "dev.pulsarfunction.pulsardjlexample.TextFunction" --dead-letter-topic chatdead --inputs "persistent://public/default/chat"   --log-topic "persistent://public/default/chatlog" --name TextProcess --namespace default --output "persistent://public/default/chatresult" --tenant public  --max-message-retries 5

bin/pulsar-admin topics create persistent://public/default/chatresult

````

## references

* https://github.com/tspannhw/FLiP-Meetup-Chat/tree/main
