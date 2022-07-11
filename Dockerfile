# build stage
FROM node:lts-alpine as build-stage
ENV PNPM_VERSION 7.5.0
RUN apk --no-cache add curl
RUN curl -sL https://unpkg.com/@pnpm/self-installer | node

WORKDIR /app
COPY . .
RUN pnpm install
RUN pnpm build:packages
RUN pnpm build
RUN pnpm store prune
RUN rm -rf ~/.pnpm-store

# production stage
FROM nginx:stable-alpine as production-stage
COPY --from=build-stage /app/dist /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
