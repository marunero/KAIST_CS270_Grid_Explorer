package test;

import test.MyQueue;

import lejos.hardware.ev3.EV3;
import lejos.hardware.lcd.TextLCD;
import lejos.hardware.motor.Motor;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3IRSensor;
import lejos.robotics.Color;
//import lejos.robotics.RegulatedMotor;
import lejos.robotics.SampleProvider;
import lejos.hardware.motor.BaseRegulatedMotor;
import lejos.hardware.BrickFinder;
import lejos.hardware.Keys;
import lejos.utility.Delay;
import lejos.hardware.Audio;



public class test_motor {
	static BaseRegulatedMotor leftMotor  = Motor.A;
	static BaseRegulatedMotor rightMotor = Motor.D;
	
	private static EV3ColorSensor color_sensor_l = new EV3ColorSensor(SensorPort.S4);
	private static EV3ColorSensor color_sensor_r = new EV3ColorSensor(SensorPort.S1);
	private static EV3IRSensor IR_sensor = new EV3IRSensor(SensorPort.S2);
	
	protected final static Audio audio = BrickFinder.getDefault().getAudio();
	
	static EV3 ev3 = (EV3) BrickFinder.getLocal();
	static TextLCD lcd = ev3.getTextLCD();
	static Keys keys = ev3.getKeys();
	
	static SampleProvider distanceMode = IR_sensor.getDistanceMode();
	static float value[] = new float[distanceMode.sampleSize()];
	public static void main(String[] args){
		int [][] map = {{0,0,0,0},{0,0,0,0},{0,0,0,0},{0,0,0,0},{0,0,0,0},{0,0,0,0}};
		int [][] robot_map = {{0,0,0,0},{0,0,0,0},{0,0,0,0},{0,0,0,0},{0,0,0,0},{0,0,0,0}};
		
		char direction = 'E';
		char want_direction = 'E';
		int [] start = {0,0};
		int [] cur_loc = {0,0};
		int [][] path = {{0,0}, {1,0},{2,0},{3,0},{4,0},{5,0},{5,1}, {5, 2}, {5, 3}, {4, 3}, {3, 3}, {2, 3}, {1, 3}, {0, 3}, {0, 2}, {1, 2}, {2, 2}, {3, 2}, {4, 2}, {4, 1}, {3, 1}, {2, 1}, {1, 1}, {0, 1}};
		int [][] AtoB;
		
		cur_loc[0] = path[0][0];
		cur_loc[1] = path[0][1];

		robot_map[0][0] = 1;
		map[0][0] = 1;

		int loc1 = 0;
		int loc2 = 1;
		if (color_check()){
			map[0][0] = 3;
		}

		int red_count = 0;
		int ob_count = 0;

		while (map_isAllPassed(map)==false){
			for (int i = 0; i < path.length; i++){
				if (cur_loc[0] == path[i][0] && cur_loc[1] == path[i][1]){
					loc1 = i;
					break;
				}
			}
			loc2 = 0;
			while (map[path[loc2][0]][path[loc2][1]] != 0){
				loc2 ++;
			}
			if ((loc2 != path.length - 1) && (map[path[loc2 - 1][0]][path[loc2 - 1][1]] == 2) && ((map[path[loc2 + 1][0]][path[loc2 + 1][1]] == 2))){
				map[path[loc2][0]][path[loc2][1]] = 4;
				continue;
			}
			map_copy(robot_map, map);
			AtoB = path(robot_map, path[loc2], path[loc1]);
			
			for (int i = 0; i < AtoB.length - 1; i ++){
				want_direction = get_dir(AtoB[i], AtoB[i + 1]); // rotate robot & get the current direction
				turn(direction, want_direction);
				direction = want_direction;

				if (box_check()){
					int [] ob_loc = obstacle_loc(cur_loc, direction);
					map[ob_loc[0]][ob_loc[1]] = 2;
					ob_count += 1;
					break;
				}
				else{
					forward();
					slight_forward();// move front
					cur_loc[0] = AtoB[i + 1][0];
					cur_loc[1] = AtoB[i + 1][1];
					map[cur_loc[0]][cur_loc[1]] = 1;
					if (color_check()){
						map[cur_loc[0]][cur_loc[1]] = 3;
						red_count += 1;
					}
				}

				if ((ob_count == 2) && (red_count == 2)){
					break;
				}
			}
			if ((ob_count == 2) && (red_count == 2)){
				break;
			}
		}

		int [][] red = new int [2][2];
		int red1 = 0;
		int [][] ob = new int [2][2];
		int ob1 = 0;
		int x = 0;
		int y = 0;
		for (int i = 0; i < map.length; i ++){
			for (int j = 0; j < map[0].length; j ++){
				if (map[i][j] == 1){
					map[i][j] = 0;
				}
				if (map[i][j] == 2){
					if (ob1 == 0){
						ob[0][0] = i;
						ob[0][1] = j;
						ob1 = 1;
					}
					else{
						ob[1][0] = i;
						ob[1][1] = j;
					}
				}
				if (map[i][j] == 3){
					if (red1 == 0){
						red[0][0] = i;
						red[0][1] = j;
						red1 = 1;
					}
					else{
						red[1][0] = i;
						red[1][1] = j;
						red1 = 2;
					}
					map[i][j] = 0;
				}
				if (map[i][j] == 4){
					x = i;
					y = j;
				}
				if ((i == map.length - 1) && (j == map[0].length - 1)){
					if (red1 == 1){
						red[1][0] = x;
						red[1][1] = y;
					}
				}
			}
			
		}	
		
		
		map_copy(robot_map, map);
		AtoB = path(robot_map, start, cur_loc);
		for (int i = 0; i < AtoB.length - 1; i ++){
			want_direction = get_dir(AtoB[i], AtoB[i + 1]); // rotate robot & get the current direction
			turn(direction, want_direction);
			direction = want_direction;

			forward();
			slight_forward();// move front
			cur_loc[0] = AtoB[i + 1][0];
			cur_loc[1] = AtoB[i + 1][1];
			map[cur_loc[0]][cur_loc[1]] = 1;
		}
		System.out.println("(" + red[0][0] + "," + red[0][1] + ",R)");
		System.out.println("(" + red[1][0] + "," + red[1][1] + ",R)");
		System.out.println("(" + ob[0][0] + "," + ob[0][1] + ",B)");
		System.out.println("(" + ob[1][0] + "," + ob[1][1] + ",B)");
		
		Delay.msDelay(50000);

	}
	
	public static char get_dir(int[] A, int[] B){
		if (A[0] == B[0]){
			if (A[1] == B[1] - 1){
				return 'N';
			}
			else{
				return 'S';
			}
		}
		if (A[1] == B[1]){
			if (A[0] == B[0] - 1){
				return 'E';
			}
			else{
				return 'W';
			}
		}
		else{
			return 'E';
		}
	}
	
	public static boolean front_is_blocked(){
		return true;
	}

	public static boolean map_isAllPassed(int [][] map){
		for (int i = 0; i < map.length; i++){
			for (int j = 0; j < map[0].length; j++){
				if (map[i][j] == 0){
					return false;
				}
			}
		}
		return true;
	}

	public static void turn(char current_dir, char want_dir){
		if(current_dir == 'N'){
			if(want_dir == 'E'){
				turn_right();
			}
			else if(want_dir == 'W'){
				turn_left();
			}
			else if(want_dir == 'S'){
				turn_left();
				turn_left();
			}
		}
		if(current_dir == 'W'){
			if(want_dir == 'N'){
				turn_right();
			}
			else if(want_dir == 'S'){
				turn_left();
			}
			else if(want_dir == 'E'){
				turn_left();
				turn_left();
			}
		}		
		if(current_dir == 'E'){
			if(want_dir == 'S'){
				turn_right();
			}
			else if(want_dir == 'N'){
				turn_left();
			}
			else if(want_dir == 'W'){
				turn_left();
				turn_left();
			}
		}
		if(current_dir == 'S'){
			if(want_dir == 'W'){
				turn_right();
			}
			else if(want_dir == 'E'){
				turn_left();
			}
			else if(want_dir == 'N'){
				turn_left();
				turn_left();
			}
		}		
	}
	
	public static void map_copy(int [][] copied_map, int [][] map){
		for (int i = 0; i < map.length; i++){
			for (int j = 0; j < map[0].length; j++){
				if (map[i][j] != 2){
					copied_map[i][j] = 0;
				}
				else{
					copied_map[i][j] = map[i][j];
				}
			}
		}
	}

	public static int [] obstacle_loc(int [] cur_loc, char direc){
		int [] loc = new int [2];
		if (direc == 'N'){
			loc[0] = cur_loc[0];
			loc[1] = cur_loc[1] + 1;
		}
		else if (direc == 'S'){
			loc[0] = cur_loc[0];
			loc[1] = cur_loc[1] - 1;
		}
		else if (direc == 'E'){
			loc[0] = cur_loc[0] + 1;
			loc[1] = cur_loc[1];
		}
		else if (direc == 'W'){
			loc[0] = cur_loc[0] - 1;
			loc[1] = cur_loc[1];
		}
		return loc;
	}
	public static int[][] path(int[][] map, int [] start, int [] end){
		// map[i][j] : (x,y) = (i,j)
		// map[i][j] = 1 (trace), 2 (obstacle)
		
		int [][] q1;

		MyQueue<int[][]> search = new MyQueue<>();
		
		int [][] q = {{start[0], start[1]}};
		map[start[0]][start[1]] = 1;
		search.enqueue(q);
		
		// Put initial path into queue
		q = search.dequeue();
		
		// until some path is located on end point
		while ((q[0][0] != end[0]) || (q[0][1] != end[1])){
			// get current position; and, add new path with each cross path
			// (x, y) -> (x, y + 1), (x, y - 1), (x + 1, y), (x - 1, y)
			// if cross path is blocked with wall or obstacle, then ignore and pass
			
			int [] current_position = new int[2]; // (x, y)
			current_position[0] = q[0][0];
			current_position[1] = q[0][1];
			
			// cross path: south
			
			// when south path is not blocked with wall
			if (current_position[1] != 0){
				// when south location (x, y - 1) is not passed
				if (map[current_position[0]][current_position[1] - 1] == 0){
					// make new path with south location (x, y - 1)
					q1 = new int[q.length + 1][2];
					for (int i = 0; i < q.length; i ++){
						q1[i + 1][0] = q[i][0];
						q1[i + 1][1] = q[i][1];
					}
					q1[0][0] = current_position[0];
					q1[0][1] = current_position[1] - 1;
					
					// add new path in queue; and, mark south location in map
					search.enqueue(q1);
					map[q1[0][0]][q1[0][1]] = 1;
				}
			}
			
			// cross path: north
			
			// when north path is not blocked with wall
			if (current_position[1] != map[0].length - 1){
				// when north location (x, y + 1) is not passed
				if (map[current_position[0]][current_position[1] + 1] == 0){
					// make new path with north location (x, y + 1)
					q1 = new int[q.length + 1][2];
					for (int i = 0; i < q.length; i ++){
						q1[i + 1][0] = q[i][0];
						q1[i + 1][1] = q[i][1];
					}
					q1[0][0] = current_position[0];
					q1[0][1] = current_position[1] + 1;
					
					// add new path in queue; and, mark north location in map
					search.enqueue(q1);
					map[q1[0][0]][q1[0][1]] = 1;
				}
			}
			
			// cross path: east
			
			// when east path is not blocked with wall
			if (current_position[0] != map.length - 1){
				// when east location (x, y + 1) is not passed
				if (map[current_position[0] + 1][current_position[1]] == 0){
					// make new path with east location (x + 1, y)
					q1 = new int[q.length + 1][2];
					for (int i = 0; i < q.length; i ++){
						q1[i + 1][0] = q[i][0];
						q1[i + 1][1] = q[i][1];
					}
					q1[0][0] = current_position[0] + 1;
					q1[0][1] = current_position[1];
					
					// add new path in queue; and, mark east location in map
					search.enqueue(q1);
					map[q1[0][0]][q1[0][1]] = 1;
				}
			}
			
			// cross path: west
			
			// when west path is not blocked with wall
			if (current_position[0] != 0){
				// when west location (x, y + 1) is not passed
				if (map[current_position[0] - 1][current_position[1]] == 0){
					// make new path with west location (x - 1, y)
					q1 = new int[q.length + 1][2];
					for (int i = 0; i < q.length; i ++){
						q1[i + 1][0] = q[i][0];
						q1[i + 1][1] = q[i][1];
					}
					q1[0][0] = current_position[0] - 1;
					q1[0][1] = current_position[1];
					
					// add new path in queue; and, mark west location in map
					search.enqueue(q1);
					map[q1[0][0]][q1[0][1]] = 1;
				}
			}
			// exception case: can't go to end point
			if (search.isEmpty()){
				break;
			}
			
			// search new path in queue
			q = search.dequeue();
		}
		return q;
	}
	public static void turn_left()
	{
		leftMotor.rotate(-265, true);
		rightMotor.rotate(265, true);
		Delay.msDelay(1000);
	}
	
	public static void turn_right()
	{
		leftMotor.rotate(265, true);
		rightMotor.rotate(-265, true);
		Delay.msDelay(1000);
		
	}
	
	public static void slight_forward()
	{
		leftMotor.setSpeed(700);
		rightMotor.setSpeed(700);
		
		leftMotor.forward();
		rightMotor.forward();
		
		Delay.msDelay(800);
		
		leftMotor.stop(true);
		rightMotor.stop(true);
		Delay.msDelay(300);
	}

	public static void forward()
	{
		leftMotor.setSpeed(270);
		rightMotor.setSpeed(270);
		
		leftMotor.forward();
		rightMotor.forward();
		
		while((color_sensor_r.getColorID()!=Color.BLACK)&&(color_sensor_l.getColorID()!=Color.BLACK))
		{
			Delay.msDelay(20);
		}		
		leftMotor.stop(true);
		rightMotor.stop(true);
		Delay.msDelay(300);
		
		while((color_sensor_r.getColorID()!=Color.BLACK))
		{
			rightMotor.rotate(10);
			leftMotor.rotate(-5);
		}
		
		while((color_sensor_l.getColorID()!=Color.BLACK))
		{
			rightMotor.rotate(-5);
			leftMotor.rotate(10);
		}
		
		
		
	}

	public static boolean color_check()
	{
		//leftMotor.rotate(80, true);
		//rightMotor.rotate(80, true);
		//Delay.msDelay(1000);
		
		if(color_sensor_l.getColorID()==Color.RED)
		{
			audio.systemSound(0);
			return true;
		}
		else
		{
			return false;
		}
	}
	
	public static boolean box_check()
	{
		distanceMode.fetchSample(value, 0);
		float centimeter = value[0];
		
		if(centimeter<=30)
		{
			audio.systemSound(0);
			return true;
		}
		else 
		{
			return false;
		}
	}
}

