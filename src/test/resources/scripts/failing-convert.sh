#!/bin/sh
# Simula um heif-convert instalado mas quebrado (ex.: biblioteca nativa ausente/incompativel),
# que aborta com um exit code != 0 sem gerar um JPEG valido. Caso real observado em producao:
# heif-convert falhando ao carregar libx265 (dyld) e saindo com exit code 134 (SIGABRT).
exit 134
