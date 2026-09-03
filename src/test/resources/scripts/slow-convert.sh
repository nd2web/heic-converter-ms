#!/bin/sh
# Simula um conversor HEIC lento, usado apenas para testar o timeout do servico.
# "exec" substitui o processo do shell pelo do sleep, entao destroyForcibly()
# no processo filho do ProcessBuilder mata diretamente o processo que esta dormindo
# (sem deixar um neto orfao rodando em segundo plano).
exec sleep 5
