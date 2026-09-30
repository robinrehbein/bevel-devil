"""Upload one Android App Bundle to internal and Alpha in a single Play edit."""

import argparse
from pathlib import Path

import google.auth
from google.auth.transport.requests import Request
import requests


API = "https://androidpublisher.googleapis.com/androidpublisher/v3/applications"
SCOPE = "https://www.googleapis.com/auth/androidpublisher"


def call(session, method, url, **kwargs):
    response = session.request(method, url, timeout=180, **kwargs)
    if not response.ok:
        raise RuntimeError(f"Play API {method} failed ({response.status_code}): {response.text}")
    return response.json()


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--bundle", type=Path, required=True)
    parser.add_argument("--package", required=True)
    parser.add_argument("--tracks", nargs="+", required=True)
    args = parser.parse_args()

    credentials, _ = google.auth.default(scopes=[SCOPE])
    credentials.refresh(Request())
    session = requests.Session()
    session.headers["Authorization"] = f"Bearer {credentials.token}"

    app_url = f"{API}/{args.package}"
    edit = call(session, "POST", f"{app_url}/edits", json={})
    edit_url = f"{app_url}/edits/{edit['id']}"

    with args.bundle.open("rb") as bundle:
        uploaded = call(
            session,
            "POST",
            edit_url.replace("androidpublisher/v3/", "upload/androidpublisher/v3/")
            + "/bundles?uploadType=media",
            data=bundle,
            headers={"Content-Type": "application/octet-stream"},
        )
    version_code = str(uploaded["versionCode"])

    for track in args.tracks:
        call(
            session,
            "PUT",
            f"{edit_url}/tracks/{track}",
            json={
                "track": track,
                "releases": [{
                    "name": f"{version_code}",
                    "versionCodes": [version_code],
                    "status": "completed",
                }],
            },
        )

    call(session, "POST", f"{edit_url}:commit", json={})
    print(f"Committed version {version_code} to {', '.join(args.tracks)}")


if __name__ == "__main__":
    main()
