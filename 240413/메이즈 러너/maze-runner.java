import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.StringTokenizer;

public class Main {
	static BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
	static StringTokenizer st;
	static int[][] map;
	static int[][] rotate_map;
	static int[] dr = { 0, 0, -1, 1 }; // 좌, 우, 상, 하
	static int[] dc = { -1, 1, 0, 0 };
	static int n, m, k;
	static Position exit = new Position(0, 0); // 출구
	static ArrayList<Position> people; // 사람들
	static int move_cnt = 0;
	static int escape_cnt = 0;

	public static void main(String[] args) throws Exception {
		st = new StringTokenizer(br.readLine());
		n = Integer.parseInt(st.nextToken()); // 미로의 크기
		m = Integer.parseInt(st.nextToken()); // 참가자 수
		k = Integer.parseInt(st.nextToken()); // 게임 시간
		map = new int[n][n];
		rotate_map = new int[n][n];
		people = new ArrayList<>();
		int current_time = 1; // 현재 시간

		for (int i = 0; i < n; i++) {
			st = new StringTokenizer(br.readLine());
			for (int j = 0; j < n; j++) {
				map[i][j] = Integer.parseInt(st.nextToken());
			}
		}

		// 참가자들 맵에 표시
		for (int i = 0; i < m; i++) {
			st = new StringTokenizer(br.readLine());
			int r = Integer.parseInt(st.nextToken()) - 1;
			int c = Integer.parseInt(st.nextToken()) - 1;
			map[r][c] += 10;
			people.add(new Position(r, c));
		}

		// 출구 위치
		st = new StringTokenizer(br.readLine());
		exit.r = Integer.parseInt(st.nextToken()) - 1;
		exit.c = Integer.parseInt(st.nextToken()) - 1;

		map[exit.r][exit.c] = 200;

		// 시간 제한 동안
		while (current_time <= k) {
			move();
			if (people.size() == 0)
				break;
			find_triangle();
			current_time++;
		}

		System.out.println(move_cnt);
		System.out.println((exit.r + 1) + " " + (exit.c + 1));
	}

	// 참가자들 이동
	public static void move() {
		int peopleCnt = people.size();

		for (int i = 0; i < peopleCnt; i++) {
			int pr = people.get(i).r;
			int pc = people.get(i).c;

			// 가장 가까운 거리로 이동할 수 있는 위치&거리
			int min_dist = Integer.MAX_VALUE;
			int cr = Integer.MAX_VALUE;
			int cc = Integer.MAX_VALUE;

			for (int j = 0; j < 4; j++) {
				int nr = pr + dr[j];
				int nc = pc + dc[j];

				if (nr < 0 || nr >= n || nc < 0 || nc >= n)
					continue;
				// 사람이 이동한 위치와 출구 사이의 거리
				int dist = Math.abs(nr - exit.r) + Math.abs(nc - exit.c);

				if (dist < min_dist) {
					min_dist = dist;
					cr = nr;
					cc = nc;
				} else if (dist == min_dist) { // 좌우 이동보다 상하 이동이 우선
					if (map[nr][nc] == 0 || (map[nr][nc] >= 10 && map[nr][nc] <= 100)) { // 이동할 수 있는 값이라면 갱신
						cr = nr;
						cc = nc;
					}
				}
			}

			// 사람 이동 완료
			if (map[cr][cc] == 0 || (map[cr][cc] >= 10 && map[cr][cc] <= 100)) { // 이동할 수 있는 구간인 경우
				map[people.get(i).r][people.get(i).c] -= 10;
				map[cr][cc] += 10;

				people.get(i).r = cr;
				people.get(i).c = cc;

				move_cnt++;
			} else if (map[cr][cc] == 200) { // 출구인 경우
				map[people.get(i).r][people.get(i).c] -= 10;
				move_cnt++;
				escape_cnt++;
			}
		}

		update_people_position();
	}

	// 제일 작은 정사각형 찾기 (완탐)
	public static void find_triangle() {
		// 출구로부터 제일 가까운 사람 찾기
		int dist = 2; // 거리
		boolean flag = false;

		while (!flag) {
			for (int i = 0; i < n; i++) {
				if (flag)
					break;
				for (int j = 0; j < n; j++) {
					if (flag)
						break;
					if (i + dist >= n || j + dist >= n)
						continue;

					int last_r = i + dist;
					int last_c = j + dist;

					// 출구가 해당 구역 안에 있는지 확인
					if (exit.r >= i && exit.r < last_r && exit.c >= j && exit.c < last_c) {
					} else {
						continue;
					}

					// 사람들이 해당 구역 안에 있는지 확인
					for (int k = 0; k < people.size(); k++) {
						int pr = people.get(k).r;
						int pc = people.get(k).c;

						if (pr >= i && pr < last_r && pc >= j && pc < last_c) {
							rotate(i, j, dist);
							flag = true;
							break;
						} else {
							continue;
						}
					}
				}
			}
			dist++;
		}
	}

	// 벽 회전 (시계 방향) 후 기존 맵에 회전 적용
	public static void rotate(int r, int c, int dist) {
		for (int i = 0; i < dist; i++) {
			for (int j = 0; j < dist; j++) {
				rotate_map[r + j][c + dist - i - 1] = map[r + i][c + j];
			}
		}

		for (int i = r; i < r + dist; i++) {
			for (int j = c; j < c + dist; j++) {
				map[i][j] = rotate_map[i][j];

				if (map[i][j] >= 1 && map[i][j] < 10) { // 벽인 경우
					map[i][j] = map[i][j] - 1;
				}
				if (map[i][j] == 200) { // 출구인 경우 출구 좌표 갱신
					exit.r = i;
					exit.c = j;
				}
			}
		}

		update_people_position();
	}

	public static void update_people_position() { // 사람 위치 갱신
		people.clear();
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				if (map[i][j] >= 10 && map[i][j] <= 100) {
					for (int k = 0; k < map[i][j] / 10; k++) {
						people.add(new Position(i, j));
					}
				}
			}
		}
	}

	public static void print(int[][] test) {
		for (int i = 0; i < n; i++) {
			System.out.println(Arrays.toString(test[i]));
		}
		System.out.println();
	}

	static class Position {
		int r;
		int c;

		public Position(int r, int c) {
			this.r = r;
			this.c = c;
		}
	}
}