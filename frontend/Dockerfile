FROM nginxinc/nginx-unprivileged:1.25-alpine
# nginx image renders /etc/nginx/templates/*.template with env vars at startup
ENV DNS_RESOLVER=kube-dns.kube-system.svc.cluster.local
COPY templates/default.conf.template /etc/nginx/templates/default.conf.template
COPY index.html /usr/share/nginx/html/index.html
EXPOSE 8080
