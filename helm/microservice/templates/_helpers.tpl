{{- define "microservice.labels" -}}
app: {{ .Values.appName }}
version: {{ .Values.version }}
app.kubernetes.io/name: {{ .Values.appName }}
app.kubernetes.io/instance: {{ .Release.Name }}
app.kubernetes.io/part-of: loan-platform
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end -}}
