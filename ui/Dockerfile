# Build stage
FROM node:20-alpine AS build
WORKDIR /app
COPY package.json package-lock.json ./
RUN npm ci
COPY . .
RUN npm run build -- --configuration production

# Runtime stage (serve static)
FROM nginx:alpine
COPY --from=build /app/dist/ui/browser /usr/share/nginx/html
# Simple Nginx config for SPA
RUN rm /etc/nginx/conf.d/default.conf
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
