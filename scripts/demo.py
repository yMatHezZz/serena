"""Demonstração HTTP com dados sintéticos. Não imprime senhas ou headers."""
import base64
import datetime as dt
import getpass
import json
import os
import uuid
import urllib.error
import urllib.parse
import urllib.request

base = os.environ.get("API_URL", "http://127.0.0.1:8082").rstrip("/")
parsed = urllib.parse.urlparse(base)
if parsed.scheme != "https" and parsed.hostname not in {"127.0.0.1", "localhost"}:
    raise SystemExit("Use HTTPS fora do computador local.")
username = os.environ.get("ADMIN_USERNAME", "admin")
password = os.environ.get("ADMIN_PASSWORD") or getpass.getpass("Senha administrativa: ")
token = base64.b64encode(f"{username}:{password}".encode()).decode()

def request(method, path, payload=None, expected=200):
    data = None if payload is None else json.dumps(payload).encode()
    req = urllib.request.Request(base + "/api/" + path, data=data, method=method,
        headers={"Authorization": "Basic " + token, "Content-Type": "application/json"})
    try:
        response = urllib.request.urlopen(req, timeout=20)
    except urllib.error.HTTPError as exc:
        response = exc
    with response:
        status = response.code
        body = response.read()
    if status != expected:
        raise SystemExit(f"Falha em {method} {path}: esperado {expected}, recebido {status}.")
    return json.loads(body) if body else None

def create(path, body):
    return request("POST", path, body, 201)["id"]

def main():
    prefix = uuid.uuid4().hex[:12]
    patient_user = create("users", {"name":"Paciente fictício", "email":f"paciente-{prefix}@example.test", "password":uuid.uuid4().hex, "status":"ACTIVE"})
    professional_user = create("users", {"name":"Profissional fictício", "email":f"profissional-{prefix}@example.test", "password":uuid.uuid4().hex, "status":"ACTIVE"})
    patient = create("patients", {"userId":patient_user, "dateOfBirth":"1990-01-01"})
    professional = create("professionals", {"userId":professional_user, "licenseNumber":f"TESTE-{prefix}", "specialty":"Psicologia demonstrativa"})
    start = (dt.datetime.now(dt.timezone.utc) + dt.timedelta(days=2)).replace(microsecond=0)
    end = start + dt.timedelta(hours=1)
    create("availability-slots", {"professionalId":professional, "startAt":start.isoformat(), "endAt":end.isoformat(), "available":True})
    booking = {"patientId":patient, "professionalId":professional, "scheduledStartAt":start.isoformat(), "scheduledEndAt":end.isoformat(), "status":"SCHEDULED"}
    consultation = create("consultations", booking)
    persisted = request("GET", "consultations/" + consultation)
    if persisted["patientId"] != patient or persisted["status"] != "SCHEDULED":
        raise SystemExit("Leitura após gravação não corresponde ao agendamento.")
    request("POST", "consultations", booking, 409)
    booking["status"] = "CANCELLED"
    request("PUT", "consultations/" + consultation, booking)
    booking["status"] = "SCHEDULED"
    replacement = create("consultations", booking)
    activity = create("activities", {"name":"Respiração demonstrativa", "description":"Cadastro fictício; exercício não executado nesta API.", "activityType":"BREATHING", "configuration":{"seconds":30}, "active":True})
    print("Aprovado: cadastro, leitura persistida, conflito 409, cancelamento e novo agendamento.")
    print("Dados sintéticos mantidos para a apresentação.")
    print("Consulta:", replacement)
    print("Atividade:", activity)

if __name__ == "__main__":
    try:
        main()
    except urllib.error.URLError as exc:
        raise SystemExit("Não foi possível acessar a API. Confira o servidor e API_URL.") from exc
