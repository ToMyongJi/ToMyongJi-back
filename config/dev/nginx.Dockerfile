FROM nginx:latest
COPY config/dev/nginx/conf.d/nginx.conf /etc/nginx/conf.d/default.conf
