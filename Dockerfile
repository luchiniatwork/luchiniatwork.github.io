FROM nginx
EXPOSE 80
COPY resources/public/ /usr/share/nginx/html
