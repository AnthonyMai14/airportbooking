/*
 * Template JAVA User Interface
 * =============================
 *
 * Database Management Systems
 * Department of Computer Science &amp; Engineering
 * University of California - Riverside
 *
 * Target DBMS: 'Postgres'
 *
 */


import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Random;

/**
 * This class defines a simple embedded SQL utility class that is designed to
 * work with PostgreSQL JDBC drivers.
 *
 */

public class AirBooking{
	//reference to physical database connection
	private Connection _connection = null;
	static BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
	
	public AirBooking(String dbname, String dbport, String user, String passwd) throws SQLException {
		System.out.print("Connecting to database...");
		try{
			// constructs the connection URL
			String url = "jdbc:postgresql://localhost:" + dbport + "/" + dbname;
			System.out.println ("Connection URL: " + url + "\n");
			
			// obtain a physical connection
	        this._connection = DriverManager.getConnection(url, user, passwd);
	        System.out.println("Done");
		}catch(Exception e){
			System.err.println("Error - Unable to Connect to Database: " + e.getMessage());
	        System.out.println("Make sure you started postgres on this machine");
	        System.exit(-1);
		}
	}
	
	/**
	 * Method to execute an update SQL statement.  Update SQL instructions
	 * includes CREATE, INSERT, UPDATE, DELETE, and DROP.
	 * 
	 * @param sql the input SQL string
	 * @throws java.sql.SQLException when update failed
	 * */
	public void executeUpdate (String sql) throws SQLException { 
		// creates a statement object
		Statement stmt = this._connection.createStatement ();

		// issues the update instruction
		stmt.executeUpdate (sql);

		// close the instruction
	    stmt.close ();
	}//end executeUpdate

	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and outputs the results to
	 * standard out.
	 * 
	 * @param query the input query string
	 * @return the number of rows returned
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public int executeQueryAndPrintResult (String query) throws SQLException {
		//creates a statement object
		Statement stmt = this._connection.createStatement ();

		//issues the query instruction
		ResultSet rs = stmt.executeQuery (query);

		/*
		 *  obtains the metadata object for the returned result set.  The metadata
		 *  contains row and column info.
		 */
		ResultSetMetaData rsmd = rs.getMetaData ();
		int numCol = rsmd.getColumnCount ();
		int rowCount = 0;
		
		//iterates through the result set and output them to standard out.
		boolean outputHeader = true;
		while (rs.next()){
			if(outputHeader){
				for(int i = 1; i <= numCol; i++){
					System.out.print(rsmd.getColumnName(i) + "\t");
			    }
			    System.out.println();
			    outputHeader = false;
			}
			for (int i=1; i<=numCol; ++i)
				System.out.print (rs.getString (i) + "\t");
			System.out.println ();
			++rowCount;
		}//end while
		stmt.close ();
		return rowCount;
	}
	
	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and returns the results as
	 * a list of records. Each record in turn is a list of attribute values
	 * 
	 * @param query the input query string
	 * @return the query result as a list of records
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public List<List<String>> executeQueryAndReturnResult (String query) throws SQLException { 
		//creates a statement object 
		Statement stmt = this._connection.createStatement (); 
		
		//issues the query instruction 
		ResultSet rs = stmt.executeQuery (query); 
	 
		/*
		 * obtains the metadata object for the returned result set.  The metadata 
		 * contains row and column info. 
		*/ 
		ResultSetMetaData rsmd = rs.getMetaData (); 
		int numCol = rsmd.getColumnCount (); 
		int rowCount = 0; 
	 
		//iterates through the result set and saves the data returned by the query. 
		boolean outputHeader = false;
		List<List<String>> result  = new ArrayList<List<String>>(); 
		while (rs.next()){
			List<String> record = new ArrayList<String>(); 
			for (int i=1; i<=numCol; ++i) 
				record.add(rs.getString (i)); 
			result.add(record); 
		}//end while 
		stmt.close (); 
		return result; 
	}//end executeQueryAndReturnResult
	
	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and returns the number of results
	 * 
	 * @param query the input query string
	 * @return the number of rows returned
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public int executeQuery (String query) throws SQLException {
		//creates a statement object
		Statement stmt = this._connection.createStatement ();

		//issues the query instruction
		ResultSet rs = stmt.executeQuery (query);

		int rowCount = 0;

		//iterates through the result set and count nuber of results.
		if(rs.next()){
			rowCount++;
		}//end while
		stmt.close ();
		return rowCount;
	}
	
	/**
	 * Method to fetch the last value from sequence. This
	 * method issues the query to the DBMS and returns the current 
	 * value of sequence used for autogenerated keys
	 * 
	 * @param sequence name of the DB sequence
	 * @return current value of a sequence
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	
	public int getCurrSeqVal(String sequence) throws SQLException {
		Statement stmt = this._connection.createStatement ();
		
		ResultSet rs = stmt.executeQuery (String.format("Select currval('%s')", sequence));
		if (rs.next()) return rs.getInt(1);
		return -1;
	}

	/**
	 * Method to close the physical connection if it is open.
	 */
	public void cleanup(){
		try{
			if (this._connection != null){
				this._connection.close ();
			}//end if
		}catch (SQLException e){
	         // ignored.
		}//end try
	}//end cleanup

	/**
	 * The main execution method
	 * 
	 * @param args the command line arguments this inclues the <mysql|pgsql> <login file>
	 */
	public static void main (String[] args) {
		if (args.length != 3) {
			System.err.println (
				"Usage: " + "java [-classpath <classpath>] " + AirBooking.class.getName () +
		            " <dbname> <port> <user>");
			return;
		}//end if
		
		AirBooking esql = null;
		
		try{
			
			try {
				Class.forName("org.postgresql.Driver");
			}catch(Exception e){

				System.out.println("Where is your PostgreSQL JDBC Driver? " + "Include in your library path!");
				e.printStackTrace();
				return;
			}
			
			String dbname = args[0];
			String dbport = args[1];
			String user = args[2];
			
			esql = new AirBooking (dbname, dbport, user, "");
			
			boolean keepon = true;
			while(keepon){
				System.out.println("MAIN MENU");
				System.out.println("---------");
				System.out.println("1. Add Passenger");
				System.out.println("2. Book Flight");
				System.out.println("3. Review Flight");
				System.out.println("4. Insert or Update Flight");
				System.out.println("5. List Flights From Origin to Destination");
				System.out.println("6. List Most Popular Destinations");
				System.out.println("7. List Highest Rated Destinations");
				System.out.println("8. List Flights to Destination in order of Duration");
				System.out.println("9. Find Number of Available Seats on a given Flight");
				System.out.println("10. < EXIT");
				
				switch (readChoice()){
					case 1: AddPassenger(esql); break;
					case 2: BookFlight(esql); break;
					case 3: TakeCustomerReview(esql); break;
					case 4: InsertOrUpdateRouteForAirline(esql); break;
					case 5: ListAvailableFlightsBetweenOriginAndDestination(esql); break;
					case 6: ListMostPopularDestinations(esql); break;
					case 7: ListHighestRatedRoutes(esql); break;
					case 8: ListFlightFromOriginToDestinationInOrderOfDuration(esql); break;
					case 9: FindNumberOfAvailableSeatsForFlight(esql); break;
					case 10: keepon = false; break;
				}
			}
		}catch(Exception e){
			System.err.println (e.getMessage ());
		}finally{
			try{
				if(esql != null) {
					System.out.print("Disconnecting from database...");
					esql.cleanup ();
					System.out.println("Done\n\nBye !");
				}//end if				
			}catch(Exception e){
				// ignored.
			}
		}
	}

	public static int readChoice() {
		int input;
		// returns only if a correct value is given.
		do {
			System.out.print("Please make your choice: ");
			try { // read the integer, parse it and break.
				input = Integer.parseInt(in.readLine());
				break;
			}catch (Exception e) {
				System.out.println("Your input is invalid!");
				continue;
			}//end try
		}while (true);
		return input;
	}//end readChoice
	
	/*
	 * Get the max id in from table
	 */
	public static String GetNextValue(AirBooking esql, String id, String table){//1.1
		String next_id = "";
		try{
			//Get last pId in database
			List<List<String>> queryResult = esql.executeQueryAndReturnResult("SELECT MAX(" + id + ") FROM " + table + ";");
			//if the database is NOT empty, get the preceding pid
			//else note that this passenger is the first entry 
			String retrieve_pid_from_query = "";
			if (!queryResult.isEmpty()) {
				retrieve_pid_from_query = queryResult.get(0).get(0);
			}
			else {
				retrieve_pid_from_query = "0";
			}
			
			int num_id = Integer.parseInt(retrieve_pid_from_query);
			num_id += 1;
			
			next_id = Integer.toString(num_id);
		}
		catch(Exception e){
			System.err.println (e.getMessage());
		}
		
		return next_id;
	}
	
	/*
	* Add a new passenger into the database. You should provide an interface that takes as
 	* input the information of a new passenger (i.e. passport number, full name, birth date e.t.c)
 	* and checks if the provided information are valid based on the constrains of the database schema.
 	*/
	public static void AddPassenger(AirBooking esql){//1
		//Add a new passenger to the database
		try {
			String pname,pNum,pdate,pcountry;
			
			//Add a new passenger to the database
			String query = "INSERT INTO Passenger VALUES(\'";
			boolean pass = true;
			do {
				System.out.print("\tEnter passenger's full name: ");
				pname = in.readLine();
				
				Pattern pattern = Pattern.compile("[a-zA-Z ]*");
				Matcher matcher = pattern.matcher(pname);
				boolean b = matcher.matches();
				
				
				if (pname.length() > 24){ 
					System.out.println("\t***ERROR: Name must be less than 24 characters (including white space)");
					pass = false;
				}
				else if (!b) {
					System.out.println("\t***ERROR: Name must be alphabet (exception is <SPACE>)");
					pass = false;
				}
				else{ pass = true; }
				
			}while(!pass);
			
			do {//BUGG: doesn't check if passport number is already taken
				System.out.print("\tEnter " + pname + "\'s passport number: ");
				pNum = in.readLine();
				
				Pattern pattern = Pattern.compile("[A-Z]*");
				Matcher matcher = pattern.matcher(pNum);
				boolean b = matcher.matches();
				
				
				if (pNum.length() != 8){ 
					System.out.println("\t***ERROR: Name must be 8 characters");
					pass = false;
				}
				else if (!b) {
					System.out.println("\t***ERROR: Name must be UPPER CASE ALPHABET");
					pass = false;
				}
				else{ pass = true; }
				
			}while(!pass);
			
			do {//Bug: doesn't check if a valid date for the month/year. Only checks if valid format & the number of dates/years
				System.out.print("\tEnter " + pname + "\'s birthday (i.e. M/D/YYYY): ");
				pdate = in.readLine();
				
				
				
				String [] pdateArr = pdate.split("/", 3);
				/*for (int i = 0; i < pdateArr.length; ++i){
					System.out.println(i + pdateArr[i]);
				}*/
				if (pdateArr.length != 3) {
					System.out.println("\t***ERROR: Invalid date format");
					pass = false;
				}
				else if(!month_valid(pdateArr[0]) || !day_valid(pdateArr[1]) || !year_valid(pdateArr[2])){
					pass = false;
				}
				else{
					pass = true;
				}
			}while(!pass);
			
			do{
				System.out.print("\tEnter " + pname + "\'s country: ");
				pcountry = in.readLine();
				
				Pattern pattern = Pattern.compile("[a-zA-Z ]*");
				Matcher matcher = pattern.matcher(pname);
				boolean b = matcher.matches();
				
				
				if (pname.length() > 24){ 
					System.out.println("\t***ERROR: Name must be less than 24 characters (including white space)");
					pass = false;
				}
				else if(!b) {
					System.out.println("\t***ERROR: Name must be alphabet (exception is <SPACE>)");
					pass = false;
				}
				else{ pass = true; }
				
			}while(!pass);
			
			String pId = GetNextValue(esql, "pID", "Passenger");
			query += pId + "\',\'" + pNum + "\',\'" + pname + "\',\'" + pdate + "\',\'" + pcountry + "\');";
			
			esql.executeUpdate(query);
			
		}	
		catch(Exception e){
			System.err.println (e.getMessage());
		}
	}
	
	public static boolean month_valid(String month) {//1.2
		//month
		boolean pass = true;
		if (month.length() == 2) {//double digit month
			//check if valid values
			Pattern pattern1 = Pattern.compile("[0-2]*");
			Matcher matcher1 = pattern1.matcher(month);
			boolean b1 = matcher1.matches();
			//convert to int
			int monthInt = Integer.parseInt(month);
			if(!b1) {
				System.out.println("\t***ERROR: Invalid month format");
				pass = false;
			}
			else if (monthInt < 10 || monthInt > 12) {
				System.out.println("\t***ERROR: Invalid month");
				pass = false;
			}
		}
		else if (month.length() == 1) {//single digit month 1.3
			//check if valid values
			Pattern pattern1 = Pattern.compile("[1-9]*");
			Matcher matcher1 = pattern1.matcher(month);
			boolean b1 = matcher1.matches();
			//convert to int
			int monthInt = Integer.parseInt(month);
			if(!b1) {
				System.out.println("\t***ERROR: Invalid month format");
				pass = false;
			}
			else {
				if (monthInt < 1 || monthInt > 9) {
					System.out.println("\t***ERROR: Invalid month");
					pass = false;
				}
				else{
					pass = true;
				}
			}
		}
		return pass;
	}
	
	public static boolean day_valid(String day) {//1.4
		boolean pass = true;
		//day
		if (day.length() == 2) {//double digit days
			//check if valid values
			Pattern pattern1 = Pattern.compile("[0-9]*");
			Matcher matcher1 = pattern1.matcher(day);
			//convert to int
			int dayInt = Integer.parseInt(day);
			boolean b1 = matcher1.matches();
			
			if(!b1) {
				System.out.println("\t***ERROR: Invalid day format");
				pass = false;
			}
			else {						
				if (dayInt < 10 || dayInt > 31) {//BUGG: doesn't check if month permits that day
					System.out.println("\t***ERROR: Invalid day");
					pass = false;
				}
				else{
					pass = true;
				}
			}
		}
		else if (day.length() == 1) {//single digit DAYS
			//check if valid values
			Pattern pattern1 = Pattern.compile("[1-9]*");
			Matcher matcher1 = pattern1.matcher(day);
			boolean b1 = matcher1.matches();
			//convert to int
			int dayInt = Integer.parseInt(day);
			if(!b1) {
				System.out.println("\t***ERROR: Invalid day format");
				pass = false;
			}
			else {
				if (dayInt < 1 || dayInt > 9) {
					System.out.println("\t***ERROR: Invalid day");
					pass = false;
				}
				else{
					pass = true;
				}
			}
		}
		return pass;
	}
	
	public static boolean year_valid(String year){
		//year
		boolean pass = true;			
		Pattern pattern3 = Pattern.compile("[0-9]*");
		Matcher matcher3 = pattern3.matcher(year);
		boolean b3 = matcher3.matches();
		//convert to int
		int yearInt = Integer.parseInt(year);
		if(!b3) {
			System.out.println("\t***ERROR: Not year format");
			pass = false;
		}
		else{
			if (yearInt < 1900 || yearInt > 2017) {
				System.out.println("\t***ERROR: Not valid year");
				pass = false;
			}
			else{
				pass = true;
			}
		}
		
		return pass;
	}

	/*
	 * Book a flight for an existing passenger. This function will enable you to book a flight
 	 * from a given origin to a given destination for an existing customer. You need to provide 
 	 * pg. 3 an interface that accepts the necessary information for booking a flight and checks if all
 	 * inputs given by the user are valid based on the defined schema and the information stored
 	 * in the database
 	 */	
	public static void BookFlight(AirBooking esql){//2
		//Book Flight for an existing customer
		String Passnum;
		int testflag = 0;
		// returns only if a correct value is given.
		do {
			System.out.println("Please Provide Your Passport Id number: ");
			try { // read the integer, parse it and break.
				//System.out.println("test");
				Passnum = in.readLine();
				System.out.println("this is passnum: " + Passnum);
				//System.out.println(Passnum.length());
				String q = "SELECT passNum,pID FROM Passenger P WHERE passNum = " + "\'"+ Passnum +"\'" + ";";
				//System.out.println("test");
				List<List<String>> currPassNum = esql.executeQueryAndReturnResult(q);
				//System.out.println("test");
				// THROWS EXECPTION WHEN LIST IS ZERO... PLEASE ADD AN IF CASE DURING
				String Pnum = currPassNum.get(0).get(0);
				String PID  = currPassNum.get(0).get(1);
				System.out.println("this is pnum: " + Pnum);
	
				if(Pnum.equals(Passnum))
				{
						String origin;
						String destination;
						System.out.println("Passport number is a valid customer number!");
						
						do {
							
							
							try {															
								System.out.println("Where are you flying from?");
								origin = in.readLine();
								System.out.println("Where are you flying to?");
								destination = in.readLine();
								do{
									try{
											String query1 = "SELECT flightnum  FROM FLIGHT F WHERE (F.origin = " + "\'"+  origin + "\'" + "AND F.destination = " + "\'" +destination +"\'" + ") AND F.seats > 0;";
											List<List<String>> potentialroute = esql.executeQueryAndReturnResult(query1);
											//System.out.println(ticket.get(0).get(0));
								            
											if(potentialroute.size() == 0 )
											{
												System.out.println("Flight origin to destination does not exist");
												
												break;
											}
											else{
													    String flightnumber = potentialroute.get(0).get(0);
													    //String pID = potentialroute.get(0).get(1);
														String letterpool = "123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
														Random rng= new Random();
														char[] hold = new char[10];
													    for (int i = 0; i < 10; i++)
														{
															hold[i] = letterpool.charAt(rng.nextInt(letterpool.length()));
														}
			                                        String Bookref = new String(hold);
													String flightnum = potentialroute.get(0).get(0);
													System.out.println("Flight has been found.");
									
													System.out.println("What date would you like to fly?[M/D/Y(E.G:11/15/2017 ) ]:");
													String date = in.readLine();
													//System.out.println(Bookref);			
													//System.out.println(date);	
													//System.out.println(flightnum);	
													
																String checkforExistingticket = "SELECT * FROM Booking F "
																						+ " WHERE F.flightNum = "       +" \'"+  flightnumber    +  "\'"
																					    + " AND F.pID = "                +" \'"+  PID             +  "\'"
																					    + " AND F.departure = "          +" \'"+  date             +  "\'";
																					    
																List<List<String>> Existingticket = esql.executeQueryAndReturnResult(checkforExistingticket);			
																			    
													if(Existingticket.size() > 0)
													{
														System.out.println("You've already booked a flight to with that date and route");
														break;
													}																		
													//booking (bookRef, departure, flightNum, pID)
													String Bookinginfo = ( "INSERT into Booking(bookRef, departure, flightNum, pID)" 
																			+ "VALUES(" +  "\'" 
																			+ Bookref         +"\'" + "," 
																			+ "\'"+ date      +"\'" + "," 
																			+ "\'"+ flightnum +"\'" + "," 
																			+ "\'"+ PID       +"\'" + ")" ) ;
													System.out.println(Bookinginfo);						
													esql.executeUpdate(Bookinginfo);
													
													String check = "SELECT bookRef FROM Booking F WHERE F.bookRef  = " + "\'" + Bookref + "\'";
											        List<List<String>> test = esql.executeQueryAndReturnResult(check);
													if(test.size() <= 0)
													{
														System.out.println("FLIGHT WAS NOT STORED");
													}
													else{
														System.out.println("FLIGHT WAS  STORED");
													}
													
													
													System.out.println("Reserving flight..... ");
											//String insert = "INSERT INTO bookings (" + "\'" +
													
											testflag = 1;		
										    break;
										}

			
									}catch(Exception e){
											System.out.println("Error");
											continue;
										}

									}while(true);
									if(testflag ==1 ){
											break;
									}
											
							}catch (Exception e) {
									System.out.println("Error");
									continue;
									}//end try
			
						}while(true);	

				}
				else
				{
					System.out.println( "Invalid Passport ID Number:Not a valid customer Passport Number");
				}				
				
				
			}catch (Exception e) {
				System.out.println("Error");
				continue;
			}//end try
			if(testflag ==1 ){
				break;
		    }	
						
		}while(true);
	}
	
	/*This function will allow you, as a travel agent to note down the reviews of
 	* passengers. You should provide an interface that allows you to insert a new record of a
 	* rating for a given flight. Make sure to check for all the necessary constraints before
 	* performing the insert. 
 	*/
	public static void TakeCustomerReview(AirBooking esql){//3
		try {
				System.out.println();
				//Insert customer review into the ratings table
				String query = "INSERT INTO Ratings VALUES(\'";
				
				boolean pass = true;
				String flightNum, pId, score, comment;
				
				do{//Retrieve flightNum
					System.out.print("Flight Number: ");
					flightNum = in.readLine();
					
					Pattern pattern = Pattern.compile("[A-Z0-9]*");
					Matcher matcher = pattern.matcher(flightNum);
					boolean b = matcher.matches();
					
					if (flightNum.length() > 8){ 
						System.out.println("\tERROR: Flight Number must be less than 8 characters");
						pass = false;
					}
					else if (!b) {
						System.out.println("\tERROR: Name must be UPPERCASE ALPHABET and/or numeric (0-9)");
						pass = false;
					}
					else if (!ExistFlight(esql, flightNum)){
						System.out.println("\tERROR: Invalid Flight!");
						pass = false;
					}
					else{ pass = true; }
					
				}while(!pass);
				
				//Retrieve pId
				do {
					do{
						System.out.print("Passenger ID: ");
						pId = in.readLine();
						
						Pattern pattern = Pattern.compile("[0-9]*");
						Matcher matcher = pattern.matcher(pId);
						boolean b = matcher.matches();
						
						if (!b) {
							System.out.println("\t***ERROR: Name must be numeric (0-9)");
							pass = false;
						}
						else if ((Integer.parseInt(GetNextValue(esql, "pId", "Passenger")) - 1) < Integer.parseInt(pId)) {
							System.out.println("\t***ERROR: Invalid Passenger!");
							pass = false;
						}
						else{ pass = true; }
						
					}while(!pass);
				
				//Check if passenger was actually on the flight
				
					if(!PassengerBookOnFlight(esql, flightNum, pId)){//LINE 819
						System.out.println("Passenger did not book this flight");
						pass = false;
					}
					else{
						pass = true;
						//Set Score
						do{
							System.out.print("How was Flight #" + flightNum + "[Rate: 0 (bad) to 5 (good)]: ");
							score = in.readLine();
							Pattern pattern = Pattern.compile("[0-5]*");
							Matcher matcher = pattern.matcher(score);
							boolean b = matcher.matches();
							if (!b) {
								System.out.println("\t***ERROR: Name must be numeric (0-5)");
								pass = false;
							}
							else{ pass = true; }
						}while(!pass);
						//Set comment
						System.out.print("Comment: ");
						comment = in.readLine();
						//execute query
						String rId = GetNextValue(esql, "rID", "Ratings");
						query += rId + "\',\'" + pId + "\',\'" + flightNum + "\',\'" + score + "\',\'" + comment + "\');";
						esql.executeUpdate(query);
					}
				}while(!pass);
		}
		catch(Exception e){
			System.err.println (e.getMessage());		
		}
	}
	public static boolean ExistFlight(AirBooking esql, String flightNum){//3.2.1
		try{	
			String query = "SELECT flightNum FROM Flight WHERE flightNum=\'" + flightNum+ "\';";
			List<List<String>> queryResult = esql.executeQueryAndReturnResult(query);
			if (queryResult.isEmpty()) { return false; }
			
			}
		catch(Exception e){
			System.err.println (e.getMessage());		
		}
		return true;
	}
	/*
	 * Check if passenger was actually on the flight
	 */
	public static boolean PassengerBookOnFlight(AirBooking esql, String flightNum, String pId){//3.2.2
		try{	
			String query = "SELECT pID FROM Booking WHERE pID=" + pId + " AND flightNum=\'" + flightNum+ "\';";
			List<List<String>> queryResult = esql.executeQueryAndReturnResult(query);
			if (queryResult.isEmpty()) { return false; }
			
			}
		catch(Exception e){
			System.err.println (e.getMessage());		
		}
		return true;
	}
	
	
	public static void InsertOrUpdateRouteForAirline(AirBooking esql){//4
		//Insert a new route for the airline
		boolean keepon = true;
		while(keepon){
			System.out.println("\n-INSERT/UPDATE ROUTE MENU-");
			System.out.println("1. Insert Route");
			System.out.println("2. Update Route (NOT FUNCTIONAL)");
			System.out.println("3. Back to MAIN MENU");
			
			switch (readChoice()){
					case 1: InsertRoute(esql);break;
					case 2: break;
					case 3: keepon = false; break;
			
			
			}
		}
		/*try{
			
		}
		catch(Exception e){
			System.err.println (e.getMessage());
		}*/
	}
	
	public static void InsertRoute(AirBooking esql){//4.1
		try{
			
			String airId, flightNum, origin, destination, plane, seats, duration;
			boolean pass = true;
			 
			String query = "INSERT INTO Flight VALUES(\'";
			System.out.println("\nEnter values (!q to EXIT): ");
			
			
			do{//Retrieve airId
				System.out.print("Enter Airline ID: ");
				airId = in.readLine();
				
				Pattern pattern = Pattern.compile("[0-9]*");
				Matcher matcher = pattern.matcher(airId);
				boolean b = matcher.matches();
				//BUGG: doesn't check if flight exist already
				if ( airId.equals("!q")) return;
				else if (!b) {
					System.out.println("\tERROR: Name must be numeric (0-9)");
					pass = false;
				}
				else if(!valid_airline(esql, airId)){
					System.out.println("\tERROR: Not Valid Airline");
					pass = false;
				}
				else{ pass = true; }
				
			}while(!pass);
			
			
			do{//Retrieve flightNum
				System.out.print("Flight Number: ");
				flightNum = in.readLine();
				
				Pattern pattern = Pattern.compile("[A-Z0-9]*");
				Matcher matcher = pattern.matcher(flightNum);
				boolean b = matcher.matches();
				
				if ( flightNum.equals("!q")) return;
				else if (flightNum.length() > 8){ 
					System.out.println("\tERROR: Flight Number must be less than 8 characters");
					pass = false;
				}
				else if (!b) {
					System.out.println("\tERROR: Name must be UPPERCASE ALPHABET and/or numeric (0-9)");
					pass = false;
				}
				else{ pass = true; }
				
			}while(!pass);
			
			do
			{
				System.out.print("Enter Origin: ");
				origin= in.readLine();
				
				Pattern pattern = Pattern.compile("[a-zA-Z ]*");
				Matcher matcher = pattern.matcher(origin);
				boolean b = matcher.matches();
				
				
				//TODO: check valid origin
				if ( origin.equals("!q")) return;
				else if(origin.length() > 16) {
					System.out.println("Invalid origin. Needs to be less than 16 letters");
					pass = false;
				}
				else if (!b){
					System.out.println("Invalid origin. Please use alpahabet only!");
					pass = false;
				}
				else{
					pass = true;
				}
			} while (!pass);
			
			do
			{
				System.out.print("Enter Destination: ");
				destination= in.readLine();
				
				Pattern pattern = Pattern.compile("[a-zA-Z ]*");
				Matcher matcher = pattern.matcher(destination);
				boolean b = matcher.matches();
				
				
				//check valid destination
				if ( destination.equals("!q")) return;
				else if(destination.length() > 16) {
					System.out.println("Invalid destination. Needs to be less than 16 letters");
					pass = false;
				}
				else if (!b){
					System.out.println("Invalid destination. Please use alpahabet only!");
					pass = false;
				}
				else{
					pass = true;
				}
			} while (!pass);
			
			do
			{
				System.out.print("Enter Plane: ");
				plane= in.readLine();
				
				//check valid plane
				if ( plane.equals("!q")) return;
				else if(plane.length() > 16) {
					System.out.println("Invalid plane. Needs to be less than 16 letters");
					pass = false;
				}
				else{
					pass = true;
				}
			} while (!pass);
	
			do
			{
				System.out.print("Enter Seats: ");
				seats= in.readLine();
				
				Pattern pattern = Pattern.compile("[0-9]*");
				Matcher matcher = pattern.matcher(seats);
				boolean b = matcher.matches();
				
				
				//check valid seats
				if ( seats.equals("!q")) return;
				else if (!b){
					System.out.println("Invalid seats. Please use numberic only!");
					pass = false;
				}
				else if(Integer.parseInt(seats) < 1 || Integer.parseInt(seats)  > 500) {
					System.out.println("Invalid seats. Seats need to be great than 0 but less than 500");
					pass = false;
				}
				else{
					pass = true;
				}
			} while (!pass);
			
			
			do
			{
				System.out.print("Enter Duration of flight: ");
				duration= in.readLine();
				
				Pattern pattern = Pattern.compile("[0-9]*");
				Matcher matcher = pattern.matcher(duration);
				boolean b = matcher.matches();
				
				//check valid duration
				if ( duration.equals("!q")) return;
				else if (!b){
					System.out.println("Invalid duration. Please use numberic only!");
					pass = false;
				}
				else if(Integer.parseInt(duration) < 1 || Integer.parseInt(duration)  > 24) {
					System.out.println("Invalid seats. Seats need to be great than -1 but less than 25");
					pass = false;
				}
				else{
					pass = true;
				}
			} while (!pass);
	
			
			//add to Flight table
			query += airId + "\',\'" + flightNum + "\',\'" + origin + "\',\'" + destination;
			query += "\',\'" + plane + "\',\'" + seats + "\',\'" + duration + "\');";
			
			esql.executeUpdate(query);
			}
			catch(Exception e){
				System.err.println (e.getMessage());
			}
	}
	
	public static boolean valid_airline(AirBooking esql, String airId){//4.1.1
		try{	
			String query = "SELECT name FROM Airline WHERE airId=\'" + airId+ "\';";
			List<List<String>> queryResult = esql.executeQueryAndReturnResult(query);
			if (queryResult.isEmpty()) { return false; }
			
			}
		catch(Exception e){
			System.err.println (e.getMessage());		
		}
		return true;
	}
	
	public static void UpdateRoute(AirBooking esql){//4.2
		return;
	}
	
	/*This function will allow you to list all available flights between two cities. A booking
 	* agent uses this information to make an informed decision when booking a given flight.
 	* You should print flight number, origin, destination, plane, and duration of flight.
	*/
	public static void ListAvailableFlightsBetweenOriginAndDestination(AirBooking esql) throws Exception{//5
		//List all flights between origin and distination (i.e. flightNum,origin,destination,plane,duration) 
		//Insert a new route for the airline
		do{
			  try{
					System.out.println("Origin Location?:");
					String origin = in.readLine();
					System.out.println("Destination Location?: ");
					String destination = in.readLine();
					String query1 = "SELECT * FROM FLIGHT F WHERE (F.origin = " 
									+ "\'" +  origin     + "\'" 
									+ " AND F.destination = " + "\'" 
									+ destination + "\'" + ") AND F.seats > 0;";
					List<List<String>> ListofFlights = esql.executeQueryAndReturnResult(query1);
								
					if(ListofFlights.size() == 0 )
					{
							System.out.println("no existing flights!");											
							break;
					}					
					for( int i = 0; i < ListofFlights.size(); i++)
					{
						    String finalprint = i+1+ "." +  "flightnum: "+ ListofFlights.get(i).get(1)  +"\n "
												   + "origin: "          + ListofFlights.get(i).get(2)  +"\n "
												   + "destination: "     + ListofFlights.get(i).get(3)  +"\n "
												   + "plane#:"           + ListofFlights.get(i).get(4)  +"\n " 
												   + "duration: "        + ListofFlights.get(i).get(6)  +"\n ";
												   //+ ". "+ ListofFlights.get(i).get(5) +"\n "
												   //+ ". "+ ListofFlights.get(i).get(6) +"\n\n";

							System.out.println(finalprint);
					}
					break;				
				}catch (Exception e){
						System.out.println("Error");
						continue;
				}
		}while(true);
	}
	/*This function will return a list of the k-most popular destinations depending on the
 	* number of flights offered to that specific destination. You should print out the name of
 	* the destination city and the number of distinct flights offered to that destination. The user
 	* should provide the value of k during runtime
	*/
	public static void ListMostPopularDestinations(AirBooking esql){//6
		//Print the k most popular destinations based on the number of flights offered tothem (i.e. destination, choices)
		boolean doneflag= true;
		do{
					
					try{	
						
						System.out.println("How many of the top Popular Destinations do you want to see?:");
						String num = in.readLine();
						int result = Integer.parseInt(num);
						String DestCount = "SELECT distinct destination,COUNT(*)" 
											+ " FROM FLIGHT GROUP BY destination"
											+ " ORDER BY COUNT(*) DESC";
						List<List<String>> DestCountTable = esql.executeQueryAndReturnResult(DestCount);
						System.out.println("LIST OF POPULAR DESTINATIONS: ");
						for( int i = 0; i < result; i++)
						{
							String finalprint = i+1 + "." +"destination:  " + DestCountTable.get(i).get(0) +" "
														+"routes there: " + DestCountTable.get(i).get(1) + "\n";
							System.out.println(finalprint);						
						}
						doneflag = false;		
					break;				
				}catch (Exception e){
						System.out.println("Error");
						continue;
				}							

		}while(doneflag);
					
	}
		
	/*This function will return a list of the k-highest rated routes based on the user ratings.
 	* You should print out the airline name, flight number, origin, destination, plane, and
 	* avg_score. The user should provide the value of k during runtime
	*/
	public static void ListHighestRatedRoutes(AirBooking esql){//7
		//List the k highest rated Routes (i.e. Airline Name, flightNum, Avg_Score)
		try{
			List<List<String>> queryResult;
			boolean flag;
			int k_num;
			do{
				System.out.print("\nEnter the number of highest rating routes you would like to view: ");
				String k = in.readLine();
				k_num = Integer.parseInt(k);
				String query = "SELECT flightNum, AVG(score) FROM Ratings GROUP BY flightNum, score ORDER BY score DESC, flightNum ASC;";
				//TODO: order by origin & destination
				queryResult = esql.executeQueryAndReturnResult(query);
				int querySize = queryResult.size();
				flag = false;
				if(querySize < k_num) {
					String diff = Integer.toString(k_num - querySize); 
					System.out.println("The selected number is " + diff + " greater than the number of flights in the database.");
					flag = true;
				}
			}while(flag);
			System.out.println("-HIGHEST RATED ROUTES-");
			for (int i = 0; i < k_num; ++i) {
				String flightNum = queryResult.get(i).get(0);
				String score_avg = queryResult.get(i).get(1).substring(0,3);
				
				String query = "SELECT airId, origin, destination, plane, seats FROM Flight WHERE flightNum = \'" + flightNum + "\';";
				List<List<String>> flightQuery = esql.executeQueryAndReturnResult(query);
				String airId = flightQuery.get(0).get(0);
				String origin = flightQuery.get(0).get(1);
				String destination = flightQuery.get(0).get(2);
				String plane = flightQuery.get(0).get(3);
				String seats = flightQuery.get(0).get(4);
				
				//just for formatting output
				if (i < 9) System.out.print("  ");
				else if (i < 99) System.out.print(" ");
				
				System.out.print(Integer.toString(i + 1));
				System.out.print(". ");
				System.out.print("Flight #:" + flightNum + " | Avg Score: " + score_avg + " | Origin: " + origin + " | Destination: " + destination);
				System.out.println(" | Plane: " + plane + " | Seats: " + seats); 
			}	
		}
		catch(Exception e){
			System.err.println (e.getMessage());		
		}

	}
	
	/*This function will return a list of a k flights for a given origin and destination in order
 	* of duration. You should print the airline, flight number, origin, destination, plane, and
 	* duration. The user should give the value of k during runtime.
 	*/
	public static void ListFlightFromOriginToDestinationInOrderOfDuration(AirBooking esql){//8
		//List flight to destination in order of duration (i.e. Airline name, flightNum, origin, destination, duration, plane)
		do{
					int doneflag = 0;
					try{	
						
						System.out.println("What is Origin location?:");
						String Origin = in.readLine();
						System.out.println("What is Destination location?:");					
						String Destination = in.readLine();
						//int result = Integer.parseInt(num);
												//String DurCount = "SELECT destination,COUNT(*) FROM FLIGHT GROUP BY destination ORDER BY COUNT(*) DESC";
										
						String DurCount =  "SELECT *"
											+" FROM FLIGHT"
											+" WHERE origin = " + "\'" + Origin+ "\'" + " AND " + "destination = " + "\'" + Destination  + "\' "
	
											+" ORDER BY duration DESC";

						List<List<String>> DurCountTable = esql.executeQueryAndReturnResult(DurCount);
						//System.out.println("passed sql durcount");
						System.out.println("LIST OF FLIGHTS IN ORDER OF DURATION: ");
						//System.out.println(DurCountTable.size());
						if(DurCountTable.size() == 0 )
						{
								System.out.println("no existing flights!");											
								break;
						}			
						for( int i = 0; i < DurCountTable.size(); i++)
						{
							//System.out.println("IM HERE");
							String finalprint = i+1 + "." +"Flight Number:   "     + DurCountTable.get(i).get(1) +"\n "
														  +"origin:          "     + DurCountTable.get(i).get(2) +"\n "
														  +"destination:     "     + DurCountTable.get(i).get(3) +"\n "
														  +"Plane            "     + DurCountTable.get(i).get(4) +"\n "
														  +"duration:        "     + DurCountTable.get(i).get(6) + "\n";
								
							System.out.println(finalprint);						
						}
	
					break;				
				}catch (Exception e){
						System.out.println("exception thrown");
						continue;
				}							

		}while(true);
					
	}
	
	/*Find the number of empty seats for a given flight on a given date. You should print flight
 	* number, origin, destination, departure date, booked seats, total number of seats, and
 	* number of available seats.
 	*/
	public static void FindNumberOfAvailableSeatsForFlight(AirBooking esql){//9
		try{
				String date, flightNum;
				boolean pass = true;
				
				do {//check if valid date
					System.out.print("Date Departure: ");
					date = in.readLine();
					String [] pdateArr = date.split("/", 3);
					/*for (int i = 0; i < pdateArr.length; ++i){
						System.out.println(i + pdateArr[i]);
					}*/
					if (pdateArr.length != 3) {
						System.out.println("\t***ERROR: Invalid date format");
						pass = false;
					}
					else if(!month_valid(pdateArr[0]) || !day_valid(pdateArr[1]) || !year_valid(pdateArr[2])){
						pass = false;
					}
					else{
						pass = true;
					}
				}while(!pass);
				
				do{//check if valid flight number
					System.out.print("Flight Number: ");
					flightNum = in.readLine();
					
					Pattern pattern = Pattern.compile("[A-Z0-9]*");
					Matcher matcher = pattern.matcher(flightNum);
					boolean b = matcher.matches();
					
					if (flightNum.length() > 8){ 
						System.out.println("\tERROR: Flight Number must be less than 8 characters");
						pass = false;
					}
					else if (!b) {
						System.out.println("\tERROR: Name must be UPPERCASE ALPHABET and/or numeric (0-9)");
						pass = false;
					}
					else if (!ExistFlight(esql, flightNum)){
						System.out.println("\tERROR: Invalid Flight!");
						pass = false;
					}
					else{ pass = true; }
					
				}while(!pass);
				
				//find number of people already booked
				String query = "SELECT COUNT(pID) FROM Booking WHERE departure='" + date +"\' AND flightNum=\'" + flightNum + "\';";
				List<List<String>> bookingQuery = esql.executeQueryAndReturnResult(query);
				String booked_seats = bookingQuery.get(0).get(0);
				
				query = "SELECT origin, destination, seats FROM Flight WHERE flightNum = \'" + flightNum + "\';";
				List<List<String>> flightQuery = esql.executeQueryAndReturnResult(query);
				String origin = flightQuery.get(0).get(0);
				String destination = flightQuery.get(0).get(1);
				String seats = flightQuery.get(0).get(2);
				
				//find available seats
				String avail_seats = Integer.toString(Integer.parseInt(seats) - Integer.parseInt(booked_seats));
				
				System.out.print("Flight #:" + flightNum +  " | Origin: " + origin + " | Destination: " + destination);
				System.out.println(" | Seats: " + seats + "| Booked Seats: " + booked_seats + " | Available Seats: " + avail_seats);
				
		}
		catch(Exception e){
			System.err.println (e.getMessage());		
		}

	}
	
}
