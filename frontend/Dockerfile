# Stage 1: Build
FROM node:22-alpine AS build
WORKDIR /app
COPY package.json package-lock.json ./
RUN npm ci
COPY . .
RUN npm run build

# Stage 2: Serve
FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf.template /etc/nginx/templates/default.conf.template
COPY swagger.html /usr/share/nginx/html/swagger.html

# Default values for local development (matches docker-compose service names)
ENV BACKEND_FOOD_HOST=ik-mat
ENV BACKEND_FOOD_PORT=8080
ENV BACKEND_ALCOHOL_HOST=ik-alkohol
ENV BACKEND_ALCOHOL_PORT=8080

EXPOSE 80
