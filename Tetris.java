import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.util.*;
import java.util.Timer;
import javax.swing.*;

public class Tetris{
	int[][] field = new int[21][12];
	final int width = 650;
	final int height = 600;
	final int f_x0 = 140;
	final int f_y0 = 70;
	final int f_w = 300;
	final int f_h = 500;
	final int n_x0 = 30;
	final int n_y0 = f_y0;
	final int h_x0 = f_x0 + f_w + 30;
	final int h_y0 = f_y0;
	final int h = 15;
	final int space = 10;
	int bx, by;
	int bx_default = 3;
	int by_default = -2;
	JFrame jf;
	BufferStrategy bs;
	Block block;
	int[][] b;
	int[][] hold = new int[4][4];
	int[] line = new int[20];
	boolean active;
	boolean gameover;
	int move_cnt;
	int set_cnt;
	int[] next = new int[14];
	int index = 0;

	final int MOVE_RATE = 30;
	final int SET_RATE = 3;
	final int LEFT = 1;
	final int RIGHT = 2;
	final int UNDER = 3;

	boolean left_key = false;
	boolean right_key = false;

	Timer timer;

	public static void main(String[] args){
		new Tetris();
	}

	Tetris(){
		jf = new JFrame("てとりす");

		jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jf.setSize(width, height);
		jf.setResizable(false);
		jf.setLocationRelativeTo(null);
		jf.setVisible(true);

		jf.setIgnoreRepaint(true);
		jf.createBufferStrategy(2);
		bs = jf.getBufferStrategy();

		init();

		jf.addKeyListener(new MyKeyListener());

		timer = new Timer();
		timer.schedule(new MyTimerTask(), 0, 16);

	}

	//初期化
	public void init(){
		for(int i = 0; i < field.length; i++){
			for(int j = 0; j < field[i].length; j++){
				if(i == 20){
					field[i][j] = 12;
				}else{
					if(j == 0){
						field[i][j] = 10;
					}else if(j == 11){
						field[i][j] = 11;
					}else{
						field[i][j] = 0;
					}
				}
			}
		}
		block = new Block();
		setNextArray(0);
		setNextArray(7);
		active = false;
		move_cnt = 0;
		set_cnt = 1;
		gameover = false;
	}

	//タスク処理
	class MyTimerTask extends TimerTask{

		@Override
		public void run(){
			if(gameover == false){
				if(active == true){
					move_cnt++;
					move_cnt %= MOVE_RATE;

					if(move_cnt == 0){
						deleteMark();
						move(UNDER, 1);

						if(set_cnt == 0){
							setBlock();
							checkLine();
							downBlock();
						}else{
							setTempBlock();
						}
					}
				}else{
					startBlock();
					if(gameOverCheck()){
						gameover = true;
						gameOverEffect();
						return;
					}
				}
				paint();
			}
		}
	}

	//描画処理
	public void paint(){
		Graphics2D g = (Graphics2D)bs.getDrawGraphics();

		//背景色で塗りつぶす
		g.setBackground(Color.white);
		g.clearRect(0, 0, jf.getWidth(), jf.getHeight());

		//ステージの背景色
		g.setColor(Color.gray);
		g.fillRect(f_x0, f_y0, f_w, f_h);

		//ステージの外枠
		g.setColor(Color.black);
		g.drawRect(f_x0, f_y0, f_w, f_h);

		//ステージの内枠
		g.setColor(Color.lightGray);
		for(int i = 1; i < 20; i++)
			g.drawLine(f_x0, f_y0 + i * (f_h / 20), f_x0 + f_w, f_y0 + i * (f_h / 20));
		for(int i = 1; i < 10; i++)
			g.drawLine(f_x0 + i * (f_w / 10), f_y0, f_x0 + i * (f_w / 10), f_y0 + f_h);

		//ブロック描画
		drawBlock(g);

		//後処理
		g.dispose();
		bs.show();
	}

	//ブロック描画
	public void drawBlock(Graphics2D g){
		//ステージ内ブロック
		for(int i = 0; i < 20; i++){
			for(int j = 1; j <= 10; j++){
				if(field[i][j] != 0){
					switch(Math.abs(field[i][j])){
					case 1:
						g.setColor(Color.cyan);
						break;
					case 2:
						g.setColor(Color.yellow);
						break;
					case 3:
						g.setColor(new Color(255, 0, 255));
						break;
					case 4:
						g.setColor(Color.orange);
						break;
					case 5:
						g.setColor(Color.blue);
						break;
					case 6:
						g.setColor(Color.green);
						break;
					case 7:
						g.setColor(Color.red);
						break;
					}
					g.fillRect(f_x0 + (j - 1) * (f_w / 10), f_y0 + i * (f_h / 20), f_w / 10, f_h / 20);
					g.setColor(Color.black);
					g.drawRect(f_x0 + (j - 1) * (f_w / 10), f_y0 + i * (f_h / 20), f_w / 10, f_h / 20);
				}
			}
		}

		//nextブロック
		int[][] tmp = new int[4][4];
		for(int i = 0; i < 5; i++){
			tmp = block.getBlock(next[(index + i) % 14]);
			for(int j = 0; j < tmp.length; j++){
				for(int k = 0; k < tmp[j].length; k++){
					if(tmp[j][k] != 0){
						switch(Math.abs(next[(index + i) % 14])){
						case 0:
							g.setColor(Color.cyan);
							break;
						case 1:
							g.setColor(Color.yellow);
							break;
						case 2:
							g.setColor(new Color(255, 0, 255));
							break;
						case 3:
							g.setColor(Color.orange);
							break;
						case 4:
							g.setColor(Color.blue);
							break;
						case 5:
							g.setColor(Color.green);
							break;
						case 6:
							g.setColor(Color.red);
							break;
						}
						g.fillRect(n_x0 + k * h, n_y0 + j * h + i * (tmp.length * h + space), h, h);
						g.setColor(Color.black);
						g.drawRect(n_x0 + k * h, n_y0 + j * h + i * (tmp.length * h + space), h, h);
					}
				}
			}
		}

		//HOLDブロック
		for(int i = 0; i < hold.length; i++){
			for(int j = 0; j < hold[i].length; j++){
				if(hold[i][j] != 0){
					switch(Math.abs(hold[i][j])){
					case 1:
						g.setColor(Color.cyan);
						break;
					case 2:
						g.setColor(Color.yellow);
						break;
					case 3:
						g.setColor(new Color(255, 0, 255));
						break;
					case 4:
						g.setColor(Color.orange);
						break;
					case 5:
						g.setColor(Color.blue);
						break;
					case 6:
						g.setColor(Color.green);
						break;
					case 7:
						g.setColor(Color.red);
						break;
					}
					g.fillRect(h_x0 + j * h, h_y0 + i * h , h, h);
					g.setColor(Color.black);
					g.drawRect(h_x0 + j * h, h_y0 + i * h , h, h);
				}
			}
		}
	}

	//ブロック出現
	public void startBlock(){
		if(index == 0 || index == 7)
			setNextArray(index);
		b = block.getBlock(next[index]);
		index++;
		index %= 14;
		bx = bx_default;
		by = by_default;
		active = true;
	}

	//next配列セット
	public void setNextArray(int num){
		num += 7;
		num %= 14;
		int[] sub = new int[7];
		for(int i = 0; i < sub.length; i++){
			int tmp;
			int j;
			do{
				tmp = (int)(Math.random() * 7);
				for(j = 0; j < i; j++){
					if(sub[j] == tmp) break;
				}
			}while(i != j);
			sub[i] = tmp;
		}
		for(int i = 0; i < sub.length; i++){
			next[num + i] = sub[i];
		}
	}

	//行が揃ってるかチェック
	public void checkLine(){
		for(int i = 0; i < line.length; i++){
			line[i] = 0;
		}

		for(int i = 19; i > 0; i--){
			int cnt = 0;
			for(int j = 1; j <= 10; j++){
				if(field[i][j] != 0)
					cnt++;
			}
			if(cnt == 0){
				return;
			}else if(cnt == 10){
				line[i] = 1;
			}else{
				line[i] = -1;
			}
		}
	}

	//行削除
	public void deleteLineBlock(){
		for(int i = 19; i > 0; i--){
			if(line[i] == 1){
				for(int j = 0; j <= 10; j++){
					field[i][j] = 0;
				}
			}
		}
	}

	//消したブロック分下にずらす
	public void downBlock(){
		int d = 0;
		for(int i = 19; i > 0; i--){
			if(line[i] == 1){
				d++;
			}else if(d != 0 && line[i] == -1){
				for(int j = 1; j <= 10; j++){
					field[i + d][j] = field[i][j];
				}
			}else if(line[i] == 0){
				for(int j = d; j > 0; j--){
					for(int k = 1; k <= 10; k++){
						field[i + j][k] = 0;
					}
				}
				return;
			}
		}
	}

	//ブロック移動
	public void move(int course, int num){
		for(int i = 0; i < b.length; i++){
			for(int j = 0; j < b[i].length; j++){
				if(course == LEFT){
					if(by + i >= 0 && b[i][j] != 0 && field[by + i][bx + j - 1] != 0)
						return;
				}
				if(course == RIGHT){
					if(by + i >= 0 && b[i][j] != 0 && field[by + i][bx + j + 1] != 0)
						return;
				}
				if(course == UNDER){
					if(b[i][j] != 0 && field[by + i + 1][bx + j] != 0){
						set_cnt++;
						set_cnt %= SET_RATE;
						return;
					}
				}
			}
		}

		if(course == LEFT)
			bx -= num;
		else if(course == RIGHT)
			bx += num;
		else if(course == UNDER){
			by += num;
			set_cnt = 1;
		}
	}

	//移動ブロックをフィールドに一時的にセット
	public void setTempBlock(){
		for(int i = 0; i < b.length; i++){
			for(int j = 0; j < b[i].length; j++){
				if(b[i][j] != 0 && by + i >= 0)
					field[by + i][bx + j] = -b[i][j];
			}
		}
	}

	//ブロックをフィールドにセット
	public void setBlock(){
		for(int i = 0; i < b.length; i++){
			for(int j = 0; j < b[i].length; j++){
				if(b[i][j] != 0 && by + i >= 0)
					field[by + i][bx + j] = b[i][j];
			}
		}
		active = false;
	}

	//ブロック移動時に跡を消す
	public void deleteMark(){
		for(int i = 0; i < b.length; i++){
			for(int j = 0; j < b[i].length; j++){
				if(bx + j >= 1 && bx + j <= 10 && by + i >= 0 && by + i < 20){
					if(field[by + i][bx + j] < 0)
						field[by + i][bx + j] = 0;
				}
			}
		}
	}

	//ブロック右回転
	public void rotate(){
		int[][] temp = new int[4][4];

		for(int i = 0; i < b.length; i++){
			for(int j = 0; j < b[i].length; j++){
				temp[i][j] = b[i][j];
			}
		}
		for(int i = 0; i < b.length; i++){
			for(int j = 0; j < b[i].length; j++){
				b[j][3 - i] = temp[i][j];
			}
		}

		if(bx <= 0){
			for(int i = 0; i < b.length; i++){
				if(b[i][0 - bx] != 0){
					for(int j = 0; j < b.length; j++){
						for(int k = 0; k < b[j].length; k++){
							b[j][k] = temp[j][k];
						}
					}
					return;
				}
			}
		}

		if(bx >= 8){
			for(int i = 0; i < b.length; i++){
				if(b[i][11 - bx] != 0){
					for(int j = 0; j < b.length; j++){
						for(int k = 0; k < b[j].length; k++){
							b[j][k] = temp[j][k];
						}
					}
					return;
				}
			}
		}
	}

	//ブロックを下まで一気に落とす
	public int dropBlock(){
		int min = field.length;
		for(int i = 0; i < b[0].length; i++){
			int up_cnt = 0;
			int down_cnt = 0;
			for(int j = b.length - 1; j >= 0; j--){
				if(b[j][i] != 0)
					break;
				up_cnt++;
			}
			for(int j = by + b.length; j < field.length; j++){
				if(bx + i >= 1 && bx + i <= 10){
					if(field[j][bx + i] != 0)
						break;
				}
				down_cnt++;
			}
			if(up_cnt < 4 && up_cnt + down_cnt < min)
				min = up_cnt + down_cnt;
		}
		return min;
	}

	//HOLDブロックと入れ替え
	public void holdChange(){
		if(active == true){
			int[][] tmp = new int[4][4];
			int cnt = 0;
			boolean ng_flag = false;
			for(int i = 0; i < tmp.length; i++){
				for(int j = 0; j < tmp[i].length; j++){
					tmp[i][j] = hold[i][j];
					hold[i][j] = b[i][j];
					b[i][j] = tmp[i][j];
					if(b[i][j] == 0) cnt++;
					else if(field[by + i][bx + j] != 0) ng_flag = true;
				}
			}
			if(ng_flag){
				for(int i = 0; i < tmp.length; i++){
					for(int j = 0; j < tmp[i].length; j++){
						tmp[i][j] = b[i][j];
						b[i][j] = hold[i][j];
						hold[i][j] = tmp[i][j];
					}					
				}
				return;
			}
			if(cnt == 16){
				//startBlock();
				setBlock();
			}
		}
	}

	//GameOverチェック
	public boolean gameOverCheck(){
		for(int i = 0; i < b.length; i++){
			for(int j = 0; j < b[i].length; j++){
				if(i + by >= 0 && field[i + by][j + bx] != 0 && b[i][j] != 0){
					gameOverEffect();
					return true;
				}
			}
		}
		return false;
	}

	//GameOverエフェクト
	public void gameOverEffect(){
		Graphics2D g = (Graphics2D)bs.getDrawGraphics();
		for(int i = 19; i >= 0; i--){
			for(int j = 1; j <= 10; j++){
				if(field[i][j] != 0){
					g.setColor(Color.lightGray);
					g.fillRect(f_x0 + (j - 1) * (f_w / 10), f_y0 + i * (f_h / 20), f_w / 10, f_h / 20);
					g.setColor(Color.black);
					g.drawRect(f_x0 + (j - 1) * (f_w / 10), f_y0 + i * (f_h / 20), f_w / 10, f_h / 20);
					try{
						Thread.sleep(80);
					}
					catch(Exception e){
					}
					bs.show();
				}
			}
		}
		g.dispose();
	}

	//キーボードアクション
	class MyKeyListener extends KeyAdapter{
		public void keyPressed(KeyEvent e){
			int keycode = e.getKeyCode();
			char key = e.getKeyChar();
			if(keycode == KeyEvent.VK_LEFT){
				deleteMark();
				move(LEFT, 1);
				setTempBlock();
			}
			if(keycode == KeyEvent.VK_RIGHT){
				deleteMark();
				move(RIGHT, 1);
				setTempBlock();
			}
			if(keycode == KeyEvent.VK_DOWN){
				deleteMark();
				move(UNDER, 1);
				setTempBlock();
			}
			if(keycode == KeyEvent.VK_SPACE){
				deleteMark();
				rotate();
				setTempBlock();
			}
			if(keycode == KeyEvent.VK_ENTER){
				if(gameover == false){
					deleteMark();
					int num = dropBlock();
					move(UNDER, num);
					setBlock();
					checkLine();
					//deleteLineBlock();
					downBlock();
				}else{
					init();
				}
			}
			if(key == 'z'){
				deleteMark();
				holdChange();
				setTempBlock();
			}
		}
	}
}

class Block {
	static int block[][][] =
		{
				{	//水色
					{0, 0, 0, 0},
					{1, 1, 1, 1},
					{0, 0, 0, 0},
					{0, 0, 0, 0},
				},

				{	//黄色
					{0, 0, 0, 0},
					{0, 2, 2, 0},
					{0, 2, 2, 0},
					{0, 0, 0, 0},
				},

				{	//紫
					{0, 0, 0, 0},
					{0, 0, 3, 0},
					{0, 3, 3, 3},
					{0, 0, 0, 0},
				},

				{	//オレンジ
					{0, 0, 0, 0},
					{0, 4, 4, 4},
					{0, 4, 0, 0},
					{0, 0, 0, 0},
				},

				{	//青
					{0, 0, 0, 0},
					{0, 5, 5, 5},
					{0, 0, 0, 5},
					{0, 0, 0, 0},
				},

				{	//緑
					{0, 0, 0, 0},
					{0, 0, 6, 6},
					{0, 6, 6, 0},
					{0, 0, 0, 0},
				},

				{	//赤
					{0, 0, 0, 0},
					{0, 7, 7, 0},
					{0, 0, 7, 7},
					{0, 0, 0, 0},
				},
		};

	public int[][] getBlock(int num){
		return block[num];
	}
}
