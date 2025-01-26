# springboot-k8s-playground

- 쿠버네티스 클러스터의 `~/.kube/config` 파일로 쿠버네티스 연결
  - 홈서버에 설치된 microk8s 기준
  - 노드 상관없이 `microk8s config` 명령어 출력을 스프링 서버의 `~/.kube/config` 로 저장
- `fabric8`
  - `~/.kube/config` 기본 설정 경로를 이용할 경우 config 설정 간편
  - 공식 `kubernetes-client` 보다 상대적으로 더 무겁지만 훨씬 간단하고 자연스러운 API 제공
- k8s 연결 시 `~/.kube/config` 방식에서 토큰 방식으로 변경
  - 외부에서 접근 가능하도록 microk8s 설정하고 config 파일에 포트포워딩 ip, port로 설정했으나 연결 안됨
  - `MasterUrl`: 포트포워딩 ip, port
  - `OauthToken`: 노드에서 기본 계정(SA) 토큰 확인
    - `kubectl -n kube-system describe secret $(kubectl -n kube-system get secret | grep default-token | awk '{print $1}')`
  - `TrustCerts`: true