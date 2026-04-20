#!/bin/bash

# Wait for RabbitMQ to be ready
sleep 10

# Create exchanges
rabbitmqctl eval "rabbit_amqqueue:declare({resource,<<"/">>,exchange,<<"edu.events">>},true,false,false,[],none)."

# Create queues
rabbitmqctl declare_queue name=email.queue durable=true
rabbitmqctl declare_queue name=notification.queue durable=true
rabbitmqctl declare_queue name=payment.queue durable=true

# Create users
rabbitmqctl add_user edu_user edu_password 2>/dev/null || true
rabbitmqctl set_permissions -p / edu_user ".*" ".*" ".*"

echo "✅ RabbitMQ initialization complete!"