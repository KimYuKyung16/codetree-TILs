import java.io.*;
import java.util.*;

// 체스판 크기 최대 40, 기사 수 최대 30
// 출력: 생존한 기사들이 총 받은 데미지의 합
public class Main {
  static BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
  static StringTokenizer st;
  static int L, N, Q; // 체스판 크기, 기사 수, 명령 수
  static int[][] map;
  static int[] dr = new int[] {-1,0,1,0}; // 상, 우, 하, 좌
  static int[] dc = new int[] {0,1,0,-1};
  static ArrayList<Knight> knights = new ArrayList<>();
  static int[] damages;

  static class Knight  {
    int r;
    int c;
    int h;
    int w;
    int k;
    int damage = 0;
    boolean alive = true;

    Knight(int r, int c, int h, int w, int k) {
      this.r = r;
      this.c = c;
      this.h = h;
      this.w = w;
      this.k = k;
    }
  }

  // 명령 수행하기
  public static void go_command(int knightIndex, int direction) {
    if (isAvilableCommand(knightIndex, direction)) { // 명령 수행 가능
      // 실제로 수행하기
      for (int i=0; i<N; i++) {
        Knight knight = knights.get(i);
        knight.r = knight.r + dr[direction];
        knight.c = knight.c + dc[direction];
        knight.damage = knight.damage + damages[i];

        if (knight.damage >= knight.k) {
          knight.alive = false;
        }
      }
    } else { // 명령 수행 불가능
      return;
    }
  }

  // 명령 수행 가능한가?
  public static boolean isAvilableCommand(int knightIndex, int direction) {
    Queue<Integer> queue = new LinkedList<>();
    queue.add(knightIndex);
    damages = new int[N];
    boolean[] ismoved = new boolean[N];
    ismoved[knightIndex] = true;

    while(!queue.isEmpty()) {
      int index = queue.poll();
      Knight knight = knights.get(index);
      int nr = knight.r + dr[direction];
      int nc = knight.c + dc[direction];

      if (nr < 0 || nr + knight.h - 1 >= L || nc < 0 || nc + knight.w - 1 >= L) return false;

      for (int i=nr; i<nr+knight.h; i++) {
        for (int j=nc; j<nc+knight.w; j++) {
          if (map[i][j] == 2) return false;
          if (map[i][j] == 1) damages[index]++;
        }
      }

      for (int i=0; i<N; i++) {
        if (ismoved[i]) continue;
        if (knights.get(i).r + knights.get(i).h < nr || knights.get(i).r > nr + knight.h - 1) continue;
        if (knights.get(i).c + knights.get(i).w < nc || knights.get(i).c > nc + knight.w - 1) continue;
      
        ismoved[i] = true;
        queue.add(i);
      }
    }

    damages[knightIndex] = 0;
    return true;
  }

  public static void main(String[] args) throws Exception {
    st = new StringTokenizer(br.readLine());
    L = Integer.parseInt(st.nextToken());
    N = Integer.parseInt(st.nextToken()); // 기사 수
    Q = Integer.parseInt(st.nextToken()); // 명령 수
    
    map = new int[L][L];
    for (int i=0; i<L; i++) {
      st = new StringTokenizer(br.readLine());
      for (int j=0; j<L; j++) {
        map[i][j] = Integer.parseInt(st.nextToken());
      }
    }

    for (int i=0; i<N; i++) {
      st = new StringTokenizer(br.readLine());
      int R = Integer.parseInt(st.nextToken());
      int C = Integer.parseInt(st.nextToken());
      int H = Integer.parseInt(st.nextToken());
      int W = Integer.parseInt(st.nextToken());
      int K = Integer.parseInt(st.nextToken()); // 체력
      knights.add(new Knight(R-1, C-1, H, W, K));
    }

    for (int i=0; i<Q; i++) {
      st = new StringTokenizer(br.readLine());
      int I = Integer.parseInt(st.nextToken())-1; // I번 기사
      int D = Integer.parseInt(st.nextToken()); // 방향 D
      go_command(I, D);
    }  
    
    int answer = 0;
    for (int i=0; i<N; i++) {
      if (knights.get(i).alive) {
        answer += knights.get(i).damage;
      }
    }
    System.out.println(answer);
  }
}
