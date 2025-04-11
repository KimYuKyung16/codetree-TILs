import java.io.*;
import java.util.*;

public class Main {
    static BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    static StringTokenizer st;
    static int N, M, K;
    static Point[][] board;
    static int[] dy = new int[] {0,1,0,-1}; // 우,하,좌,상
    static int[] dx = new int[] {1,0,-1,0};
    static int[] shell_dy = new int[] {-1,-1,-1,0,1,1,1,0};
    static int[] shell_dx = new int[] {-1,0,1,1,1,0,-1,-1};

    public static class Point {
      int power;
      int attackTime;
      boolean attack;

      Point(int power, int attackTime, boolean attack) {
        this.power = power;
        this.attackTime = attackTime;
        this.attack = attack;
      }
    }

    public static class Turret {
      int y;
      int x;
      int power;
      int attackTime;

      Turret(int y, int x, int power, int attackTime) {
        this.y = y;
        this.x = x;
        this.power = power;
        this.attackTime = attackTime;
      }
    }

    public static class LaserPoint {
      int y;
      int x;
      int direction;
      LaserPoint node;

      LaserPoint(int y, int x, int direction, LaserPoint node) {
        this.y = y;
        this.x = x;
        this.direction = direction;
        this.node = node;
      }
    }

    // 1. 공격자 선정
    public static Turret set_attacker() {
      Turret selected = null;

      for (int i=0; i<N; i++) {
        for (int j=0; j<M; j++) {
          int power = board[i][j].power;
          int attackTime = board[i][j].attackTime;
          if (power == 0) continue; // 이미 죽은 포탑
          if (selected == null) {
            selected = new Turret(i, j, power, attackTime);
          } else {
            if (power < selected.power) {
              selected = new Turret(i, j, power, attackTime);
            } else if (power == selected.power) { // 공격력이 같을 경우
              if (attackTime < selected.attackTime) { // 공격한지 더 오래됐을 경우
                selected = new Turret(i, j, power, attackTime);
              } else if (attackTime == selected.attackTime) {
                if (i + j > selected.y + selected.x) { // 행과 열의 합이 더 큰 경우
                  selected = new Turret(i, j, power, attackTime);
                } else if (i+j == selected.y + selected.x) { // 행과 열의 합이 같을 경우
                  if (j > selected.x) { // 열 값이 더 큰 경우
                    selected = new Turret(i, j, power, attackTime);
                  }
                }
              }
            }
          }
        }
      }
    
      // 핸디캡 적용
      board[selected.y][selected.x].power = board[selected.y][selected.x].power + N + M;
      selected.power = selected.power + N + M;
      return selected;
    }

    // 2. 공격자의 공격
    public static void attack(Turret attacker) {
      Turret strongTurret = pick_strong_turret(attacker); // 가장 강한 포탑

      boolean isAvailableLaserAttack = attack_laser(attacker, strongTurret); // 레이저 공격 가능 여부

      if (!isAvailableLaserAttack) { // 레이저 공격 불가능한 경우
        // 포탄 공격 진행
        attack_shell(attacker, strongTurret);
      } 

      fix(attacker, strongTurret);
    }

    // 레이저 공격
    public static boolean attack_laser(Turret attacker, Turret strongTurret) {
      Queue<LaserPoint> queue = new LinkedList<>();
      queue.add(new LaserPoint(attacker.y, attacker.x, -1, null));
      boolean[][] visited = new boolean[N][M];
      visited[attacker.y][attacker.x] = true;

      while(!queue.isEmpty()) {
        LaserPoint current = queue.poll();
        int cy = current.y;
        int cx = current.x;
        int direction = current.direction;
        
        // 최단 경로가 있는 경우
        if (cy == strongTurret.y && cx == strongTurret.x) {
          // 공격 대상 포탑
          board[strongTurret.y][strongTurret.x].power = board[strongTurret.y][strongTurret.x].power - attacker.power < 0 ? 0 : board[strongTurret.y][strongTurret.x].power - attacker.power;
          
          LaserPoint init = current;
          while(true) { // 공격 경로에 있는 포탑들
            LaserPoint prev = init.node;
            if (prev == null || (prev.y == attacker.y && prev.x == attacker.x)) {
              break;
            }

            int newPower = board[prev.y][prev.x].power - (int)attacker.power/2;
            if (newPower < 0) {
              board[prev.y][prev.x].power = 0;
            } else {
              board[prev.y][prev.x].power = newPower;
            }
            board[prev.y][prev.x].attack = true;
            
            init = prev;
          }
  
          return true;
        }
        
        for (int d=0; d<4; d++) {
          int ny = cy + dy[d];
          int nx = cx + dx[d];

          if (ny < 0 || ny >= N || nx < 0 || nx >= M) { // 막힌 방향으로 진행하려고 할 경우
            if (ny < 0) {
              ny = N-1;
            } else if (ny >= N) {
              ny = 0;
            } else if (nx < 0) {
              nx = M-1;
            } else if (nx >= M) {
              nx = 0;
            }
          }

          if (visited[ny][nx]) continue;
          if (board[ny][nx].power == 0) continue; // 부서진 포탑 지나기 불가능
          
          visited[ny][nx] = true;
          if (direction == -1) { // 초기값
            queue.add(new LaserPoint(ny, nx, d, current));
          } else { // 이미 direction 있는 경우
            queue.add(new LaserPoint(ny, nx, direction, current));
          }
        }
      }
    
      return false;
    }

    // 포탄 공격
    public static void attack_shell(Turret attacker, Turret strongTurret) {
      // 공격 대상 포탑
      board[strongTurret.y][strongTurret.x].power = board[strongTurret.y][strongTurret.x].power - attacker.power < 0 ? 0 : board[strongTurret.y][strongTurret.x].power - attacker.power;
      
      // 공격 경로 포탑
      for (int d=0; d<8; d++) {
        int ny = strongTurret.y + shell_dy[d];
        int nx = strongTurret.x + shell_dx[d];

        if (ny < 0 || ny >= N || nx < 0 || nx >= M) { // 가장자리
          if (ny < 0) {
            ny = N;
          } else if (ny >= N) {
            ny = 0;
          }

          if (nx < 0) {
            nx = M;
          } else if (nx >= M) {
            nx = 0;
          }
        }

        int newPower = board[ny][nx].power - (int) (attacker.power / 2);
        board[ny][nx].power = newPower < 0 ? 0 : newPower;
        board[ny][nx].attack = true;
      }
    }

    // 가장 강한 포탑 선정
    public static Turret pick_strong_turret(Turret attacker) {
      Turret selected = null;

      for (int i=0; i<N; i++) {
        for (int j=0; j<M; j++) {
          int power = board[i][j].power;
          int attackTime = board[i][j].attackTime;
          if (i == attacker.y && j == attacker.x) continue; // 
          if (power == 0) continue; // 이미 죽은 포탑
          if (selected == null) {
            selected = new Turret(i, j, power, attackTime);
          } else {
            if (power > selected.power) {
              selected = new Turret(i, j, power, attackTime);
            } else if (power == selected.power) { // 공격력이 같을 경우
              if (attackTime > selected.attackTime) { // 공격한지 더 오래됐을 경우
                selected = new Turret(i, j, power, attackTime);
              } else if (attackTime == selected.attackTime) {
                if (i + j < selected.y + selected.x) { // 행과 열의 합이 더 작은 경우
                  selected = new Turret(i, j, power, attackTime);
                } else if (i+j == selected.y + selected.x) { // 행과 열의 합이 같을 경우
                  if (j < selected.x) { // 열 값이 더 작은 경우
                    selected = new Turret(i, j, power, attackTime);
                  }
                }
              }
            }
          }
        }
      }
    
      return selected;
    }

    // 포탑 정비
    public static void fix(Turret attacker, Turret strongTurret) {
      for (int i=0; i<N; i++) {
        for (int j=0; j<M; j++) {
          if (board[i][j].power == 0) continue;
          if (i == attacker.y && j == attacker.x) {
            continue;
          }
          if (i == strongTurret.y && j == strongTurret.x) {
            continue;
          }
          if (board[i][j].attack) { // 공격 받은 애
            continue;
          }
          board[i][j].power = board[i][j].power+1;
        }
      }
    }

    public static void init() throws Exception {
        st = new StringTokenizer(br.readLine());
        N = Integer.parseInt(st.nextToken());
        M = Integer.parseInt(st.nextToken());
        K = Integer.parseInt(st.nextToken()); // 턴 수
        board = new Point[N][M];

        for (int i=0; i<N; i++) {
          st = new StringTokenizer(br.readLine());
          for (int j=0; j<N; j++) {
            board[i][j] = new Point(Integer.parseInt(st.nextToken()), 0, false);
          }
        }
    }

    public static void answer() {
      int answer = 0;
      for (int i=0; i<N; i++) {
        for (int j=0; j<M; j++) {
          if (board[i][j].power > answer) {
            answer = board[i][j].power;
          }
        }
      }

      System.out.println(answer);
    }

    public static void main(String[] args) throws Exception {
      init();

      int count = 0;
      while(!(count == K)) {
        count++;
        Turret attacker = set_attacker(); // 공격자
        // 공격자는 시간 0, 공격자 제외 시간 +1
        for (int i=0; i<N; i++) {
          for (int j=0; j<M; j++) {
            if (i == attacker.y && j == attacker.x) {
              board[i][j].attackTime = 0;
              continue;
            }
            board[i][j].attackTime = board[i][j].attackTime+1;
          }
        }
        attack(attacker);

      }
      answer();


    }
  }
