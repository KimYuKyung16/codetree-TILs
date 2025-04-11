import java.io.*;
import java.util.*;

// 1번 사람들이 편의점 방향으로 이동
// 2번 편의점 도착하면 멈춤, 해당 칸 이용 불가능
// 3번 베이스 캠프 들어감.

public class Main {
  static BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
  static StringTokenizer st;
  static int N, M, T;
  static int[][] map;
  static Person[] people;
  static Position[] camps;
  static int[] dy = new int[] {-1,0,0,1};
  static int[] dx = new int[] {0,-1,1,0};

  static class Position {
    int y;
    int x;

    Position(int y, int x) {
      this.y = y;
      this.x = x;
    }
  }

  static class Person {
    int y, x;
    Position store; // 가려는 편의점
    boolean camp; // 편의점과 가장 가까이 있는 베이스 캠프
    boolean isArrived;

    Person(int y, int x, Position store) {
      this.y = y;
      this.x =x;
      this.store = store;
    }
  }

  public static void init() throws Exception {
    st = new StringTokenizer(br.readLine());
    N = Integer.parseInt(st.nextToken()); // 격자 크기
    M = Integer.parseInt(st.nextToken()); // 사람 수
    map = new int[N][N];

    camps = new Position[N*N-M+1];
    int campIndex = 0;
    // 0은 빈 공간, 1은 베이스캠프
    for (int i=0; i<N; i++) {
      st = new StringTokenizer(br.readLine());
      for (int j=0; j<N; j++) {
        int input = Integer.parseInt(st.nextToken());
        if (input == 1) { // 베이스캠프일 경우
          camps[campIndex++] = new Position(i-1, j-1);
        }
        map[i][j] = input;
      }
    }

    people = new Person[M+1];
    for (int i=0; i<M; i++) {
      st = new StringTokenizer(br.readLine());
      int y = Integer.parseInt(st.nextToken());
      int x = Integer.parseInt(st.nextToken());
      Position store = new Position(y-1, x-1);
      Person person = new Person(-1,-1,store);
      people[i] = person;
    }
  }

  static void move() {
    T = 0;
    while(true) {
      T++;
      // 1번: 본인이 가고 싶은 편의점 방향을 향해 1칸 움직임.
      for (int i=0; i<M; i++) {
        Person person = people[i];
        if (!person.isArrived && person.camp) {
          int direction = getDirection(person);
          person.y = person.y + dy[direction];
          person.x = person.x + dx[direction];
        }
      }
      // 2번: 편의점에 도착한다면 해당 편의점에서 멈추고, 해당 칸 지나갈 수 없음.
      for (int i=0; i<M; i++) {
        Person person = people[i];
        if (person.y == person.store.y && person.x == person.store.x) { // 편의점 도착
          person.isArrived = true;
          map[person.store.y][person.store.x] = -1; // 지나갈 수 없는 칸
        }
      }

      // 3번: 자신이 가고 싶은 편의점과 가장 가까이 있는 베이스 캠프로 이동
      for (int i=0; i<M; i++) {
        Person person = people[i];
        if (T == i+1) {
          // 베이스캠프로 이동
          int[] camp = getBaseCamp(person.store);
          map[camp[0]][camp[1]] = -1;
          person.y = camp[0];
          person.x = camp[1];
          person.camp = true; // 캠프에 도착했다는 의미
        }
      }

      // 모든 사람들이 편의점에 도착했는가?
      if (getAllPeopleArrivedStore()) {
        System.out.println(T);
        return;
      }
    }
  }

  public static boolean getAllPeopleArrivedStore() {
    for (int i=0; i<M; i++) {
      if (!people[i].isArrived) {
        return false;
      }
    }
    return true;
  }

  // 가장 가까운 베이스캠프로 이동
  // 우선순위: 행이 작은 -> 열이 작은
  public static int[] getBaseCamp(Position store) {
    Queue<int[]> queue = new LinkedList<>();
    queue.add(new int[] {store.y, store.x, 1});
    boolean[][] visited = new boolean[N][N];
    int[] selectedCamp = new int[] {Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE};

    while(!queue.isEmpty()) {
      int[] current = queue.poll();
      
      if (current[2] > selectedCamp[2]) continue;

      if (map[current[0]][current[1]] == 1) { // 베이스캠프에 도착했을 경우 
        if (current[2] == selectedCamp[2]) { // 걸리는 정도가 같은 경우
          if (current[0] < selectedCamp[0]) { // 행이 더 작은 경우
            selectedCamp[0] = current[0];
            selectedCamp[1] = current[1];
          } else if (current[0] == selectedCamp[0]) {  // 행이 같은 경우
            if (current[1] < selectedCamp[1]) { // 열이 더 작은 경우
              selectedCamp[0] = current[0];
              selectedCamp[1] = current[1];
            }
          } 
        } else if (current[2] < selectedCamp[2]) { // 더 적게 걸리는 경우
          selectedCamp[0] = current[0];
          selectedCamp[1] = current[1];
          selectedCamp[2] = current[2];
        }
        continue;
      }

      for (int d=0; d<4; d++) {
        int ny = current[0] + dy[d];
        int nx = current[1] + dx[d];
        if (ny < 0 || ny >= N || nx < 0 || nx >= N) continue;
        if (visited[ny][nx]) continue;
        if (map[ny][nx] == -1) continue;
        visited[ny][nx] = true;
        queue.add(new int[] {ny, nx, current[2]+1});
      }
    }

    return selectedCamp;
  }

  // 편의점 이동 방향 찾기
  // 우선순위: 상, 좌, 우, 하
  public static int getDirection(Person person) {
    Queue<int[]> queue = new LinkedList<>(); // y, x, direction
    queue.add(new int[] {person.y, person.x, -1});
    boolean[][] visited = new boolean[N][N];
    visited[person.y][person.x] = true; 

    while(!queue.isEmpty()) {
      int[] current = queue.poll();
      int cy = current[0];
      int cx = current[1];
      int cd = current[2];
      if (cy == person.store.y && cx == person.store.x) {
        return cd; // 가장 처음 이동한 방향을 반환
      }
      for (int d=0; d<4; d++) {
        int ny = cy + dy[d];
        int nx = cx + dx[d];
        if (ny < 0 || ny >= N || nx < 0 || nx >= N) continue;
        if (map[ny][nx] == -1) continue;
        if (visited[ny][nx]) continue;

        visited[ny][nx] = true;
        if (cd == -1) { // 아직 초기값이 없는 경우
          queue.add(new int[] {ny, nx, d});
        } else {
          queue.add(new int[] {ny, nx, cd}); // 기존에 이동하던 방향값을 그대로 넣기
        }
      }
    }
    return -1;
  }

  public static void main(String[] args) throws Exception {
    init();
    move();
  }
}
