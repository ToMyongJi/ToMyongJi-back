FROM nginx:latest
COPY config/prod/nginx.conf /etc/nginx/conf.d/default.conf