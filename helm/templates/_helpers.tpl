{{/* Expand the name of the chart. */}}
{{- define "okrs-backend.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/* Create a default fully qualified app name. */}}
{{- define "okrs-backend.fullname" -}}
{{- printf "%s-%s" .Release.Name (include "okrs-backend.name" .) | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/* Common labels applied to everything */}}
{{- define "okrs-backend.labels" -}}
helm.sh/chart: {{ printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" }}
{{ include "okrs-backend.selectorLabels" . }}
app.kubernetes.io/version: {{ .Values.image.tag | default .Chart.AppVersion | quote }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end }}

{{/* Selector labels used by Services to find Pods */}}
{{- define "okrs-backend.selectorLabels" -}}
app.kubernetes.io/name: {{ include "okrs-backend.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end }}