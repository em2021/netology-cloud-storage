FROM node:19-alpine AS builder
ENV repo=https://github.com/netology-code/jd-homeworks/tree/master/diploma/netology-diplom-frontend
ENV wdir=netology-diplom-frontend
RUN apk update && apk upgrade && apk add git && apk add py-pip && pip install github-clone && ghclone $repo && cd $wdir
WORKDIR /netology-diplom-frontend
RUN npm install

FROM node:19-alpine
COPY --from=builder /netology-diplom-frontend netology-diplom-frontend/
WORKDIR /netology-diplom-frontend
EXPOSE 8080
ENTRYPOINT ["npm", "run", "serve"]