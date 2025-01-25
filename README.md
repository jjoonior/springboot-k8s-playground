# springboot-k8s-playground

- 쿠버네티스 클러스터의 `~/.kube/config` 파일로 쿠버네티스 연결
  - 홈서버에 설치된 microk8s 기준 
    - 노드 상관없이 `microk8s config` 명령어 출력을 스프링 서버의 `~/.kube/config` 로 저장
- `fabric8`
  - `~/.kube/config` 기본 설정 경로를 이용할 경우 config 설정 간편
  - 공식 `kubernetes-client` 보다 상대적으로 더 무겁지만 훨씬 간단하고 자연스러운 API 제공