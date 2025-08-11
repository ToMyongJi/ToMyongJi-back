FROM nginx:latest
COPY config/dev/nginx.conf /etc/nginx/conf.d/default.conf
