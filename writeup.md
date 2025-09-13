# Insured Assurance - CI/CD Integration (GitHub Actions → Jenkins → Apache Tomcat)

## Project goal
Create a GitHub Actions CI/CD workflow that builds a Java web application and invokes a Jenkins job to perform deployment to an Apache Tomcat server.

## Architecture (high level)
1. Developer pushes code to GitHub (main branch).
2. GitHub Actions triggers, checks out code, builds (Maven), runs tests, and optionally uploads the built artifact.
3. GitHub Actions triggers a remote Jenkins job (via build token or API) passing the commit SHA or artifact location.
4. Jenkins job checks out the repo (or downloads artifact), builds (or reuses artifact), and deploys the WAR to Apache Tomcat (using Tomcat Manager or SSH).

## Prerequisites
- GitHub repository with this project.
- Jenkins instance reachable from GitHub Actions (public URL or via VPN).
- Jenkins job created with either:
  - **Freestyle/Parameterized job** with "Trigger builds remotely" token, or
  - **Pipeline job** that accepts parameters (e.g. `GIT_COMMIT`) and uses Jenkins credentials to deploy to Tomcat.
- Tomcat server with Manager app enabled (or SSH access to Tomcat server).
- GitHub repository Secrets to set:
  - `JENKINS_URL` (e.g. `https://jenkins.example.com`)
  - `JENKINS_USER` (username)
  - `JENKINS_API_TOKEN` (API token for the Jenkins user)
  - `JENKINS_JOB` (job name, e.g. `deploy-to-tomcat`)
  - `JENKINS_BUILD_TOKEN` (optional, if using build token)
  - `TOMCAT_MANAGER_USER`, `TOMCAT_MANAGER_PASS` (for Tomcat Manager deployments) — these are used by Jenkins credentials ideally

## Folder structure (provided in the package)
- `.github/workflows/ci-cd.yml` — GitHub Actions workflow that builds and triggers Jenkins.
- `jenkins/Jenkinsfile` — Example declarative pipeline for the Jenkins job.
- `src/` — Sample Java webapp (WAR) with simple servlet.
- `pom.xml` — Maven configuration to produce a WAR.
- `writeup.md` — This document.

## GitHub Actions workflow (summary)
- Trigger: `push` to `main`.
- Steps:
  1. Checkout
  2. Set up JDK 11
  3. Cache Maven dependencies
  4. `mvn -B -DskipTests=false package`
  5. Upload artifact (optional)
  6. Trigger Jenkins remote job with commit SHA and/or artifact URL:
     ```bash
     curl -X POST "${{ secrets.JENKINS_URL }}/job/${{ secrets.JENKINS_JOB }}/buildWithParameters?GIT_COMMIT=${{ github.sha }}&token=${{ secrets.JENKINS_BUILD_TOKEN }}" -u "${{ secrets.JENKINS_USER }}:${{ secrets.JENKINS_API_TOKEN }}"
     ```

## Jenkins pipeline (summary)
- Accepts parameter `GIT_COMMIT`.
- Checks out repo at specified commit (or main).
- Runs `mvn clean package`.
- Deploys `target/*.war` to Tomcat via Tomcat Manager:
  ```bash
  curl --upload-file target/app.war "http://$TOMCAT_HOST:8080/manager/text/deploy?path=/insured-assurance&update=true" --user $TOMCAT_USER:$TOMCAT_PASS
  ```

## Security & Best Practices
- Store credentials in GitHub Secrets and Jenkins Credentials (not in plaintext).
- Use dedicated service accounts for Jenkins with minimum privileges.
- If Jenkins is not publicly reachable, use a webhook relay or GitHub Actions self-hosted runner inside the same network.
- Prefer artifact storage (Nexus/Artifactory) if artifacts must be shared across systems; Jenkins can then fetch the artifact directly.

## How to test locally
1. Install Java 11+, Maven, Tomcat.
2. Run `mvn package` in project root — generates `target/insured-assurance.war`.
3. Deploy the WAR manually by copying to Tomcat `webapps/` or use Tomcat Manager.
4. Visit `http://<tomcat-host>:8080/insured-assurance/` to see the Hello page.

## Notes
- The included Jenkinsfile uses Jenkins credentials (set `TOMCAT_CREDENTIALS_ID`) to supply Tomcat Manager username/password.
- The GitHub Actions workflow demonstrates how to trigger Jenkins remotely; you can adapt it to pass artifact URLs or use the Jenkins REST API with CSRF crumb if enabled.

