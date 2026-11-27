# ETAPA 1: COMPILACION DE ANGULAR
FROM node:22-alpine AS build

WORKDIR /app

COPY package*.json ./

RUN npm ci

COPY . .

RUN npm run build

# ETAPA 2: SERVIDOR WEB NGINX
FROM nginx:alpine

# nginx reemplaza ${PORT} y ${BACKEND_URL} de la plantilla al arrancar el contenedor
ENV PORT=80
ENV BACKEND_URL=http://backend:8080

COPY nginx.conf.template /etc/nginx/templates/default.conf.template

COPY --from=build /app/dist/bibliotech-v2fe/browser/ /usr/share/nginx/html/

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
