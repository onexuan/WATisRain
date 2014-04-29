package com.lucky.watisrain.backend;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import com.lucky.watisrain.backend.data.Building;
import com.lucky.watisrain.backend.data.Location;
import com.lucky.watisrain.backend.data.Map;
import com.lucky.watisrain.backend.data.Path;
import com.lucky.watisrain.backend.data.Waypoint;

/**
 * MapFactory generates a Map object given external input data.
 */
public class MapFactory {
	
	
	/**
	 * Read a Map object given a Scanner pointed to the stream
	 */
	public static Map readMapFromScanner(Scanner prescanner){
		
		// Preprocess to remove blank lines and comment lines
		StringBuilder sb = new StringBuilder();
		while(prescanner.hasNextLine()){
			String line = prescanner.nextLine();
			if(line.trim().isEmpty() || line.charAt(0) == '#') continue;
			sb.append(line + "\n");
		}
		
		// Actual scanner, don't worry about extraneous stuff anymore
		Scanner scanner = new Scanner(sb.toString());
		
		Map map = new Map();
		
		while(scanner.hasNext()){
			
			String command = scanner.next();
			
			if(command.equals("location")){
				
				// location is actually a Building
				
				String name = scanner.next();
				int pos_x = scanner.nextInt();
				int pos_y = scanner.nextInt();
				int num_floors = 1;
				int main_floor = 1;
				
				while(true){
					if(!scanner.hasNext()) break;
					String s = scanner.next();
					if(s.equals(";")) break;
					
					if(s.equals("floors")){
						num_floors = scanner.nextInt();
					}
					
					if(s.equals("main_floor")){
						main_floor = scanner.nextInt();
					}
				}
				
				Building building = new Building(name, new Waypoint(pos_x, pos_y), num_floors, main_floor);
				
				map.addBuilding(building);
			}
			
			else if(command.equals("passive_location")){
				
				// Passive locations
				
				String name = scanner.next();
				int pos_x = scanner.nextInt();
				int pos_y = scanner.nextInt();
				map.addPassiveLocation(new Location(name,pos_x,pos_y, false));
			}
			
			else if(command.equals("path")){
				
				// Paths
				
				String name1 = scanner.next();
				String name2 = scanner.next();
				
				// Handle main floors
				Building build1 = map.getBuildingByID(name1);
				Building build2 = map.getBuildingByID(name2);
				
				int connect_floor1 = build1 == null ? 1 : build1.getMainFloorNumber();
				int connect_floor2 = build2 == null ? 1 : build2.getMainFloorNumber();
				boolean indoors = false;
				
				// Read waypoints
				List<Waypoint> waypoints = new ArrayList<>();
				
				// Read until semicolon
				while(true){
					if(!scanner.hasNext()) break;
					String s = scanner.next();
					if(s.equals(";")) break;
					
					if(s.equals("p")){
						// add waypoint
						int wx = scanner.nextInt();
						int wy = scanner.nextInt();
						
						waypoints.add(new Waypoint(wx,wy));
					}
					
					if(s.equals("inside")){
						indoors = true;
					}
					
					if(s.equals("connects")){
						connect_floor1 = scanner.nextInt();
						connect_floor2 = scanner.nextInt();
					}
				}
				
				
				Location loc1 = map.getLocationByID(Util.makeBuildingAndFloor(name1, connect_floor1));
				Location loc2 = map.getLocationByID(Util.makeBuildingAndFloor(name2, connect_floor2));
				
				// Prepend the initial location
				waypoints.add(0,loc1.getPostion());
				
				waypoints.add(loc2.getPostion());
				
				Path path = new Path(loc1,loc2);
				path.setWaypoints(waypoints);
				path.setIndoors(indoors);
				
				map.addPath(path);
				
			}
		}
		
		scanner.close();
		
		return map;
	}
	

	/**
	 * Create a Map object from a specified File
	 */
	public static Map readMapFromFile(File infile)
			throws FileNotFoundException{
		
		return readMapFromScanner(
				new Scanner(
					new FileInputStream(infile)));
		
	}
	
	
	public static Map readMapFromStream(InputStream stream){
		return readMapFromScanner(new Scanner(stream));
	}
	
	
}
