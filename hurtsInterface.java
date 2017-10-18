import java.sql.*;
import java.io.*;
import java.util.*;
import java.util.stream.IntStream;

public class hurtsInterface 
{
	/* FIELDS */

	static Connection con;
	static Statement s;
	static Scanner reader = new Scanner(System.in);

	/* MAIN METHOD */

	public static void main (String[] arg) throws SQLException 
	{
		try 
		{
			Class.forName ("oracle.jdbc.driver.OracleDriver");
		}
		catch (Exception exc) 
		{
			System.out.print("No suitable driver found.");
			con.close();
			System.exit(0);
		}

		hurtsInterface hInt = new hurtsInterface();
		
		/* LOGIN */
		hInt.oracleLogin();
		
		/* CUSTOMER INTERFACE */
		
		// are you in the system?
		boolean registered = hInt.custCheck();

		// if so, let's pull up your account info
		if (registered)
		{
			hInt.checkCustLicense();
		}
		// if not, would you like to?
		else
		{
			hInt.wantToRegister();			
		}

		// now that you're registered, do you want to rent a car?
		hInt.wantToRent();
		con.close();
		System.exit(0);
	}
	
	/* HELPER METHODS*/

	/* Process-driving helper methods */
	
	public void oracleLogin() throws SQLException
	{
		String pass;
		int numWrong = 0;
		boolean success = false;
		
		while (!success)
		{
			System.out.print("Password for msj219" + ":\n> ");
			pass = reader.nextLine();			
			try 
			{
				con = DriverManager.getConnection("jdbc:oracle:thin:@edgar0.cse.lehigh.edu:1521:cse241","msj219",pass);
				success = true;
			}
			catch (Exception exc)
			{
				if ((numWrong++) == 4)
				{
					System.out.print("\n");
					System.out.print("You are an imposter");
					System.exit(0);
					con.close();
				}
				System.out.println("\nIncorrect user id or password, try again.");
				
				if ((numWrong) == 4)
				{
					System.out.println("THIS IS YOUR FINAL ALLOWED ATTEMPT!");
				}
			}
		}
	}

	public boolean custCheck()
	{
		System.out.print("\nHave you registered with Hurts before? \nPlease type \"Yes\" or \"No\":\n> ");
		return yesOrNo();
	}

	public void wantToRegister() throws SQLException
	{
		System.out.print("\nDo you want to register with Hurts? We have the best used cars around! \nPlease type \"Yes\" or \"No\":\n> ");
		if (yesOrNo())
		{
			registerCustomer();
		}
		else
		{
			bye("\nSorry to hear that! Hopefully you come back, we're determined to win you over!");
		}
	}

	public void wantToRent() throws SQLException
	{
		System.out.print("\nDo you want to rent a beautiful Hurts rental car? There's got to be one with your name on it! \nPlease type \"Yes\" or \"No\":\n> ");
		if (yesOrNo())
		{
			rentVehicle();
		}
		else
		{
			bye("\nSorry to hear that! We'll be waiting, whenever you need your next sweet ride!");
		}
	}

	public void registerCustomer() throws SQLException
	{
		System.out.print("\nThank you for choosing to register with Hurts! \nIf at any point in this process you wish to cancel, please type \"cancel\". \n");

		List<String> attributes = Arrays.asList(enterCustName(),enterCustAddress(),enterCustLicense());
		int[] order = new int[] {2,0,1};

		String newCustomerInsert = insertHelp("customer",attributes,order);
		String firstName = untilSpace(attributes.get(0));
		String fullName = attributes.get(0);
		String license = attributes.get(2);
		String address = attributes.get(1);
		String cleanAddress = address.substring(0,address.length() - 6) + ", " + address.substring(address.length() - 2);

		tryInsert(newCustomerInsert, "\nCongratulations " + firstName +"! You've joined the Hurts team!\n\nHere is the information we have on file for you:\n\nName ..................... " + fullName + "\nDriver's license # ....... " + license + "\nAddress .................. " + cleanAddress + "\n\nWe look forward to doing business with you!");
	}


	/* Static, code-simplifying helper methods */


	public static void rentVehicle() throws SQLException
	{
		List<String> vehTypes = Arrays.asList("SUV","minicar","subcompact","compact","full size","sports car","minivan","truck");

		System.out.println("\nGreat, it's time to find a car that suits you!");
		giveOptions("vehicle", "vehicle_type", vehTypes);
	}

	public static void giveOptions(String tableName, String column, List<String> types) throws SQLException
	{
		List<String> searchOptions = new ArrayList<String>();

		System.out.println("\nWhat kind of " + column + " are you looking for? Return the number corresponding to your preference.");
		System.out.println("\nGreat! Here are the choices:");
		
		int[] choices = new int[types.size()];
		for(int i = 0; i < types.size(); i++)
		{
			choices[i] = i+1;
			String subhead = "(" + (i+1) + ") ";
			System.out.print(subhead + types.get(i).toUpperCase() + "\n");
		}
		System.out.print("\n> ");
		
		int index = chooseInt(choices.length);
		String chosenType = types.get(index-1);

		searchOptions = getLicensesForAttribute(chosenType,"vehicle_type");
		
		String chosenTypeUpper = chosenType.toUpperCase();
		
		System.out.println("\nGreat choice!\n");
		System.out.println("Our selection of (" + index + ") " + chosenTypeUpper + " is as follows:\n");
		
		System.out.println("tableName: " + tableName + ",   column: " + column + ",   chosenType: " + chosenType);
		
		searchOptions = searchVehTable(tableName, column, chosenType, "Sorry, we don't have any vehicles of that type at this time");
		System.out.println("Search options(models): " + searchOptions.toString());

		String modelRequest = toCamelCase(askModel(searchOptions));
  		searchOptions = printModels(modelRequest);

		String licenseRequest = askLicense(searchOptions).toUpperCase();
		
		boolean rent = confirmRental(licenseRequest);

		if (!rent)
		{
			System.out.println("Ok. Can we interest you in another rental option?\nPlease type \"Yes\" or \"No\":\n>");
			if (yesOrNo())
			{
				rentVehicle();
			}
			else
			{
				bye("Maybe some other day then!");
			}
		}
	}

	public static void executeRental(String custLicense, String vehLicense, int loc, int baseRate)
	{
		System.out.println("What is your 16-digit credit card number?\n> ");
		String ccNo = enterNumDigits(16);

		System.out.println("How many days would you like to rent this car for? The maximum number of days is 99:\n> ");
		String days = enterMaxDigits(3);	

		try
		{
			s = con.createStatement();
			//Insert into customer (license, name, address, state) values ('85299527', 'TEMPLATE', 'TEMPLATE', 'FL');

			String insert = "Insert into rental (cust_license, veh_license, pickup_loc, credit_card, baseRate, length) values (";
			insert = insert + "'" + custLicense + "',";
			insert = insert + " '" + vehLicense + "',";
			insert = insert + " " + loc + ",";
			insert = insert + " '" + ccNo + "',";
			insert = insert + " " + baseRate + ",";
			insert = insert + " " + days + ")";

			System.out.println(insert);
			s.executeUpdate(insert);

			int totalCost = (Integer.parseInt(days))*baseRate;
			System.out.println("Your total comes out to: $" + totalCost + "\n See you in " + days + " days!");
			System.out.println("Thank you for doing business with us!");
		}
		catch (Exception e)
		{
			System.out.println(e.getMessage());
		}
	}

	public static boolean confirmRental(String license)
	{
		boolean rented = false;

		System.out.println("Aha! That is exactly the car with your name on it!\nPlease confirm your rental:\n");

		String license_plate = "";
		String make = "";
		String model = "";
		String year = "";
		String vehicle_type = "";
		String baseRate = "";
		String address = "";
		String state = "";
		String loc_type = "";
		String loc_ID = "";		

		ResultSet res;
		try
		{
			s = con.createStatement();
			String query = "select * from vehicle natural join parks natural join location where license_plate = '" + license + "'";
			res = s.executeQuery(query);

			if (!res.next())
			{
				System.out.println("For some reason we can't get that car for you ");
			}
			else
			{
				do
				{
					license_plate = res.getString("license_plate");
					make = res.getString("make");
					model = res.getString("model");
					year = res.getString("year");
					vehicle_type = res.getString("vehicle_type");
					baseRate = res.getString("baseRate");
					address = res.getString("address");
					state = res.getString("state");
					loc_type = res.getString("loc_type");
					loc_ID = res.getString("loc_ID");
				}
				while(res.next());

				System.out.println("Here is information for the car you've selected:\n\n");

				System.out.println("CAR INFO:\n");
				System.out.println("License Plate # ......... " + license_plate);
				System.out.println("Make .................... " + make);
				System.out.println("Model ................... " + model);
				System.out.println("Year .................... " + year);
				System.out.println("Vehicle Type ............ " + vehicle_type);

				System.out.println("LOCATION INFO:\n");
				System.out.println("Address: ................ " + address);
				System.out.println("License Plate #: ........ " + state);
				System.out.println("Location type............ " + loc_type);

				System.out.println("FINANCIAL INFO:\n");
				System.out.println("Daily Base Rate:......... $" + baseRate);
			}
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage());
		}

		System.out.println("\nWould you like to confirm this rental? \nPlease type \"Yes\" or \"No\":\n>");
		 
		if (!yesOrNo())
		{
			return false;
		}
		else
		{
			System.out.println("Perfect! We just need to confirm your license number and provide your credit card information and we'll be all set!\n");		
			
			String custLicense = verifyCustLicense();
			
			executeRental(custLicense, license_plate, Integer.parseInt(loc_ID), Integer.parseInt(baseRate));
			return true;
		}
	}

	public static String verifyCustLicense()
	{
		ResultSet res;
		boolean success = false;

		while (!success)
		{
			String license = enterCustLicense();
			try
			{
				s = con.createStatement();
				String query = "select * from customer where license = '" + license + "'";
				res = s.executeQuery(query);
				if (!res.next())
				{
					System.out.print("No matches. Try again.\n");
				}
				else
				{
					do
					{
						String name = res.getString("name");
						String address = res.getString("address");
						String state = res.getString("state");
						System.out.print("\n\nDoes this information match your account? license number:\n\nName ..................... " + name + "\nDriver's license # ....... " + license + "\nAddress .................. " + address + ", " + state + "\n");
						if (yesOrNo())
						{
							return license;
						}
						else
						{
							System.out.print("Ok. Try again then.\n");
						}
					}
					while (res.next());
				}
			}
			catch (Exception e)
			{
				System.out.println(e.getMessage());
			}
		}
		return "";
	}

	public static List<String> getLicensesForAttribute(String att, String column)
	{
		ResultSet res;
		List<String> licenses = new ArrayList<String>();
		try
		{
			s = con.createStatement();
			String query = "select * from vehicle where " + column + " = '" + att.toLowerCase() + "'";
			System.out.println(query);
			res = s.executeQuery(query);

			if (!res.next())
			{
				System.out.println("We don't have any models of that type at our locations at the moment!");
			}
			else
			{
				do
				{
					String l = res.getString("license_plate");
					licenses.add(l);

				}
				while(res.next());
			}
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage());
		}
		
		return licenses;		
	}

	public static String askLicense(List<String> searchOptions)
	{
		System.out.print("\nPlease type the name of the license plate of the  you'd like to rent:\n> ");

		return inListUpper(searchOptions,"\nTry again. That is not one of the options you were given:\n> ");
	}

	public static List<String> printModels (String modelRequest)
	{
		ResultSet res;
		List<String> licenses = new ArrayList<String>();

		try
		{
			s = con.createStatement();
			String query = "select distinct license_plate, make, model, address, state from vehicle natural join parks natural join location where model = '" + modelRequest + "'";
			System.out.println(query);
			res = s.executeQuery(query);

			if (!res.next())
			{
				System.out.println("We don't models of that type at our locations at the moment!");
			}
			else
			{
				return licenses = outPutModelsToCust(res);
			}
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage());
		}
		
		return licenses;
	}

	public static List<String> outPutModelsToCust(ResultSet res) throws SQLException
	{
		List<String> licenses = new ArrayList<String>();
		System.out.println("Here are the choices:\n");
		System.out.printf("%-16s%-12s%-22s%-40s%-4s\n", "License Plate", "Make", "Model", "Address", "State");
		System.out.println("---------------    ----------       ------------           -------------------------------      ------");
		do
		{
			licenses.add(res.getString("license_plate"));
			System.out.printf ("%-16s%-12s%-22s%-40s%-4s\n", res.getString("license_plate"), res.getString("make"), res.getString("model"), res.getString("address"), res.getString("state"));
		}
		while(res.next());

		return licenses;
	}

	public static String askModel(List<String> models)
	{
		System.out.print("\nPlease type the name of the model you'd like to rent:\n> ");

		return inListCamel(models,"\nTry again. That is not a one of the models you were given:\n> ");
	}

	public static List<String> searchVehTable(String tableName, String column, String attribute, String notFoundMessage)
	{
		ResultSet res;
		
		try
		{
			s = con.createStatement();
			String query = "select distinct make, model, baseRate from " + tableName + " where " + column + " = '" + attribute + "'";
			res = s.executeQuery(query);
			if (!res.next())
			{
				System.out.println(notFoundMessage);
			}
			else
			{
				return outPutVehToCust(res);
			}
		}
		catch(Exception e) 
		{
			System.out.println(e.getMessage());
		}
		return null;
	}

	public static List<String> outPutVehToCust(ResultSet res) throws SQLException
	{
		List<String> licenses = new ArrayList<String>();
		System.out.printf("%-12s%-22s%-6s\n", "Make", "Model", "Rate");
		System.out.println("----       ------------          --------");
		do
		{
			licenses.add(res.getString("model"));
			System.out.printf ("%-12s%-22s%-6s\n", res.getString("make"), res.getString("model"),"$" + res.getString("baseRate"));
		}
		while(res.next());

		return licenses;
	}

	public static int chooseInt(int max)
	{
		String input;

		while (true)
		{
			input = reader.nextLine();
			int num = Integer.parseInt(input);

			if (!isNumString(input) || (num < 1 || num > max))
			{
				System.out.print("Try again. You did not enter a number in thhe correct range:\n> ");
			}
			else
			{
				return num;
			}
		}

	}

	public static void bye(String farewell) throws SQLException
	{
		System.out.println(farewell + "\nBye!");
		con.close();
		System.exit(0);
	}


	public void tryInsert(String insert, String successMessage) throws SQLException
	{
		try
		{
			s = con.createStatement();	
			s.executeUpdate(insert);

			System.out.println(successMessage);
		}
		catch (Exception e)
		{
			System.out.println(e.getMessage());
		}
		finally
		{
			if (s!=null)
			{
				con.close();
			}
		}
	}


	public static String inListUpper(List<String> strList, String message)
	{
		boolean success = false;
		String input;

		while (!success)
		{
			input = reader.nextLine();
			if (success = (strList.contains(input.toUpperCase())))
			{
				return input;
			}
			else
			{
				System.out.print(message);
			}
		}

		return "";
	}

	public static String inListCamel(List<String> strList, String message)
	{
		boolean success = false;
		String input;

		while (!success)
		{
			input = reader.nextLine();
			if (success = (strList.contains(toCamelCase(input))))
			{
				return input;
			}
			else
			{
				System.out.print(message);
			}
		}

		return "";
	}

	public static String isYesNo()
	{
		List<String> list = Arrays.asList("YES","NO");
		String str = "Try again. It's either \"Yes\" or \"No\":\n> ";
		return inListUpper(list,str);
	}

	public static boolean yesOrNo()
	{
		return isYesNo().toUpperCase().equals("YES");

	}

/*
	public static void registerCustLicense()
	{
		boolean success = false;

		while(!success)
		{
			try 
			{
				return enterCustLicense();
			}
			catch (Exception e)
			{
				System.out.println("There is already a customer in our system with that license number! Are you sure you were not already registered with us?\nWe'll ask one more time: Are you a returning customer of ours?\n> ");
				if (yesOrNo()) 
				{
					System.out.println("Ok, so enter you license number:\n> ");

				}
				else
				{
					System.out.println("Alright, we'll take your word for it. Try again!");
					registerCustLicense();
				}
			}
		}
	}
*/

	public void checkCustLicense()
	{
		ResultSet res;
		int numWrong = 0;
		boolean success = false;

		while (!success)
		{
			try
			{
				s = con.createStatement();
				String license = enterCustLicense(); 
				String query = "select * from customer where license = '" + license + "'";
				res = s.executeQuery(query);
				if (!res.next())
				{
					if ((numWrong++) == 4)
					{
						System.out.println("\nThere seems to be some sort of a misunderstanding. We have no accounts under any of the license numbers you've provided.");
						this.wantToRegister();
						success = true;		
					}
					if ((numWrong) == 3)
					{
						System.out.print("\nAre you sure you are a registered customer of Hurts?\n");
					}
					else
					{
						System.out.print("No matches. Try again.\n");
					}
				}
				else
				{
					do
					{
						String name = res.getString("name");
						String address = res.getString("address");
						String state = res.getString("state");
						System.out.print("\n\nHere is the information we have on file for that license number:\n\nName ..................... " + name + "\nDriver's license # ....... " + license + "\nAddress .................. " + address + ", " + state + "\n");
					}
					while (res.next());
					success = true;
				}
			}
			catch (Exception e)
			{
				System.out.println(e.getMessage());
			}
		}
	}

	public static String enterCustLicense()
	{
		System.out.print("\nPlease enter your 8-digit drivers license number:\n> ");
		return enterNumDigits(8);

	}

	public static String enterCustName()
	{
		System.out.print("\nPlease enter your first and last name:\n> ");
		return enterName();
	}
	
	public static String enterCustAddress()
	{
		System.out.println("\nPlease enter your address followed by a comma and your state abbreviation. For example, \"123 Dope Road, NY\".");
		System.out.println("However, note there are specific styles for P.O. Boxes and Apartments. Please follow these templates:");
		System.out.println("  - \"P.O. box\" [Box No. goes here], [Rest of address goes here]");
		System.out.print("  - \"Ap #\"[Ap No. goes here] [Rest of address goes here]\n>  ");
		return enterAddrress();
	}

	public static String insertHelp(String tableName, List<String> attributes)
	{
		String insertQuery = "Insert into " + tableName + " values (";

		for (int i = 0; i < attributes.size() - 1; i++) 
		{
			insertQuery += add(attributes.get(i));
		}

		return insertQuery + addEnd(attributes.get(attributes.size() - 1));
	}

	public static String insertHelp(String tableName, List<String> attributes, int[] order)
	{
		String insertQuery = "Insert into " + tableName + " values (";

		for (int i = 0; i < attributes.size() - 1; i++) 
		{
			insertQuery += add(attributes.get(order[i]));
		}

		return insertQuery + addEnd(attributes.get(order[attributes.size()-1]));
	}

	public static String enterNumDigits(int digits)
	{
		String input;

		while (true)
		{
			input = reader.nextLine();

			if (!isNumString(input) || input.length() != digits)
			{
				System.out.print("Try again. You did not enter a " + digits + "-digit number:\n> ");
			}
			else
			{
				return input;
			}
		}
	}

	public static String enterMaxDigits(int maxDigits)
	{
		String input;

		while (true)
		{
			input = reader.nextLine();

			if (!isNumString(input))
			{
				System.out.println("Try again. Remember to enter in numerical form:\n> ");
			}

			else if (input.length() >= maxDigits)
			{
				System.out.print("Try again. " + input + " days is too long to rent a car:\n> ");
			}
			else
			{
				return input;
			}
		}
	}

	public static String enterName()
	{
		String input;

		while (true)
		{
			input = reader.nextLine();

			if (numSpaces(input) != 1)
			{
				System.out.print("Try again. You did not enter a first and last name.\n> ");
			}
			else if (input.length() > 30 || !isNameString(input))
			{
				System.out.print("You may only use letters (up to 30) for your name.\n> ");				
			}
			else
			{
				return toCamelCase(input);
			}
		}
	}

	public static String enterAddrress()
	{
		String input;

		while (true)
		{
			input = reader.nextLine();

			if (input.length() < 4) 
			{
				System.out.print("Try again. This is not an address. It's not even close.\n> ");
			}

			else
			{
				int commaAt = input.length() - 4;
				String address = input.substring(0,commaAt);
				String state = input.substring(commaAt);


				char lastAddressChar = address.charAt(address.length() - 1);
				boolean formatRight = isAlphaChar(lastAddressChar) || lastAddressChar != '.';

				if (!state.substring(0,2).equals(", ") || !formatRight)
				{
					System.out.print("Try again. The address was formatted incorrectly. Remember to place a comma between your address and your state.\n> ");
				}
				else if (!isAddressString(address) || address.length() > 40)
				{
					System.out.print("Try again. The address you entered either included an unexpected character or was too long.\n> ");
				}
				else if (!isStateAbr(state.substring(2)))
				{
					System.out.print("Try. again. You did not provide a valid state.\n> ");
				}
				else
				{
					return toCamelCase(address) + "', '" + state.substring(2).toUpperCase();
				}
			}
		}		
	}

	public static boolean isStateAbr(String str)
	{
		List<String> states = Arrays.asList("AK", "AL", "AR", "AZ", "CA", "CO", "CT", "DC", "DE", "FL", "GA", "HI", "IA", "ID", "IL", "IN", "KS", "KY", "LA", "MA", "MD", "ME", "MI", "MN", "MO", "MS", "MT", "NC", "ND", "NE", "NH", "NJ", "NM", "NV", "NY", "OH", "OK", "OR", "PA", "RI", "SC", "SD", "TN", "TX", "UT", "VA", "VT", "WA", "WI", "WV", "WY");
		return states.contains(str.toUpperCase());
	}

	// String checks


	public static boolean isNumString(String str) 
	{ 
		char[] chars = str.toCharArray();
		for (char c: chars)
		{
			if(!isNumChar(c))
			{
				return false;
			}
		}
		return true;
	}

	public static boolean isAlphaString(String str) 
	{
    	char[] chars = str.toCharArray();
    	for (char c : chars) 
    	{
        	if(!isAlphaChar(c)) 
        	{
            	return false;
        	}
    	}
    	return true;
	}

	public static boolean isAlphaNumString(String str)
	{
    	char[] chars = str.toCharArray();
    	for (char c : chars) 
    	{
        	if(!isAlphaNumChar(c)) 
        	{
            	return false;
        	}
    	}
    	return true;
	}

	public static boolean isNameString(String str) 
	{
    	char[] chars = str.toCharArray();
    	for (char c : chars) 
    	{
        	if(!(isAlphaChar(c) || c == ' ')) 
        	{
            	return false;
        	}
    	}
    	return true;
	}

	public static boolean isAddressString(String str)
	{
		char[] chars = str.toCharArray();
		List<Character> okChars = Arrays.asList('-','.',',','#',' ');
		char first = chars[0];
		char last  = chars[chars.length - 1];
		boolean poBox = false;
		boolean apt = false;

		if (!isAlphaNumChar(first))
		{
			return false;
		}

		if(!isAlphaNumChar(last) && last != '.')
		{
			return false;
		}

		int i = 0;

		if(Character.toUpperCase(first) == 'P' && chars[1] == '.')
		{
			if(chars.length < 12)
			{
				return false;
			}
			else
			{
				if(!str.substring(0,9).equalsIgnoreCase("P.O. Box "))
				{
					return false;
				}
				else if (!isNumChar(chars[9]))
				{
					return false;
				}
			}

			i = 9;
			poBox = true;
		}

		else if (Character.toUpperCase(first) == 'A')
		{
			if (Character.toUpperCase(chars[1]) == 'P' && chars[2] == ' ')
			{
				if(chars[3] != '#' || !isNumChar(chars[4]))
				{
					return false;
				}

				else
				{
					i = 4;
					apt = true;
				}
			}
		}

		while (i < chars.length - 1)
		{
			char curr = chars[i];
			char next = chars[i+1];

			/*
			while (poBox)
			{
				i++;
				System.out.println(i + ":  " + curr);
				if (!isNumChar(curr))
				{
					if (curr != ',')
					{
						return false;
					}
					else
					{
						poBox = false;
					}
				}
			}

			while (apt)
			{
				i++;
				if (!isNumChar(curr))
				{
					if (curr != '-' && curr != ' ')
					{
						return false;
					}
					else
					{
						apt = false;
					}
				}
			}
			*/

			if (!isAddressChar(curr))
			{
				return false;
			}

			else if (curr == ',' && next != ' ')
			{
				return false;
			}

			else if (curr == '.' && next != ' ')
			{
				return false;
			}

			else if (chars[i] == '-')
			{
				char prev = chars[i-1];
				boolean bothNum = isNumChar(prev) && isNumChar(next);
				boolean bothAlpha = isAlphaChar(prev) && isAlphaChar(next);

				if (!bothAlpha && !bothNum)
				{
					return false;
				}
			}


			else if (isAlphaChar(curr) && (isNumChar(next)))
			{
				return false;
			}

			else if (isNumChar(curr) && !isNumChar(next))
			{
				if (next == '.' || isAlphaChar(next)) 
				{
					return false;
				}
			}

			i++;
		}

		return true;
	}

	// Char checks

	public static boolean isNumChar(char ch)
	{
		return Character.isDigit(ch);
	}

	public static boolean isAlphaChar(char ch)
	{
		return Character.isLetter(ch);
	}

	public static boolean isAlphaNumChar(char ch)
	{
		return isAlphaChar(ch) || isNumChar(ch);
	}

	public static boolean isAddressChar(char ch)
	{
		List<Character> okChars = Arrays.asList('-','.',',',' ');
		return okChars.contains(ch) || isAlphaNumChar(ch);
	}

	public static int numSpaces(String str) 
	{
    	char[] chars = str.toCharArray();
    	int count = 0;

    	for (char c : chars) 
    	{
    		if (c == ' ')
    		{
    			count++;
    		}
    	}
    	return count;
	}

	public static String toCamelCase(String str)
	{
		char[] chars = str.toCharArray();

		chars[0] = Character.toUpperCase(chars[0]);

		for (int i = 0; i < chars.length - 1; i++)
		{
			if (chars[i] == ' ' || chars[i] == '.')
			{
				chars[i+1] = Character.toUpperCase(chars[i+1]);
			}
			else
			{
				chars[i+1] = Character.toLowerCase(chars[i+1]);
			}
		}
		return String.valueOf(chars);
	}

	public static String add (String input)
	{
		return "'" + input + "', ";
	}

	public static String addEnd (String input)
	{
		return "'" + input + "')";
	}

	public static String untilSpace(String str)
	{
    	char[] chars = str.toCharArray();
    	String out = "";

    	for (char c : chars) 
    	{
    		if (c == ' ')
    		{
    			return out;
    		}
    		out += c;
    	}
    	return out;		
	}

	/*
	void searchName()
	{
		ResultSet res;
		String subName;
		String query = "select id, name from instructor where name like ";
		boolean success = false;
		
		while (!success)
		{
			System.out.print("Input name search substring: ");
			subName = reader.nextLine();
			subName = "'" + "%" + subName + "%" + "'";
			query = query + subName;
			
			try
			{
				state = con.createStatement();
				res = state.executeQuery(query);
				if (!res.nextLine())
				{
					System.out.print("No matches. Try again. ");
					this.searchName();
				}
				if (res.nextLine())
				{
					System.out.print("Here is a list of all matching IDs");
					while (res.nextLine())
					{
						System.out.print("  ");
						System.out.printf("%-5s %s %n", res.getString("id"), res.getString("name"));
					}
				}
				success = true;
			}
			catch (Exception exc)
			{
				System.out.print("The single quote is not allowed in a search string. Please re-enter.");
				success = true;
				this.searchName();
			}
		}
	}
	
	void searchID()
	{
		ResultSet res;
		String id;
		String query;
		String querypt1 = "select distinct course.dept_name as Department, teaches.course_id as CNO,course.title as Title, teaches.sec_id as Sec ,teaches.semester as Semester,teaches.year as Year, (select count(distinct takes.id) from takes where takes.course_id = teaches.course_id and takes.sec_id = teaches.sec_id) as Enrollment from teaches, course, takes where (teaches.id = ";
		String querypt2 = ") and (teaches.course_id = course.course_id) order by Department, CNO, Year, Semester";
		boolean success = false;
		
		System.out.print("Enter the instructor ID for the instructor whose teaching record you seek.");
		
		while (!success)
		{
			System.out.print("Please enter an integer between 0 and 99999: ");
			id = reader.nextLine();
			query = querypt1 + id + querypt2;
			
			try
			{
				state = con.createStatement();
				res = state.executeQuery(query);
				if (!res.nextLine())
				{
					this.searchID();
				}
				if (res.nextLine())
				{
					System.out.print("\n");
					System.out.printf("Teaching record for instructor " + id);
					System.out.print("\n");
					System.out.printf("%-20s%-10s%-40s%-5s%-10s%-5s%-12s\n", "Department", "CNO", "Title", "Sec", "Semester", "Year", "Enrollment");
					//System.out.printf(res.getString("id") + " " + res.getString("name"));
					while (res.nextLine())
					{
						System.out.printf ("%-20s%-10s%-40s%-5s%-10s%-5s%-12s\n", res.getString("Department") ,  res.getString("CNO"), res.getString("Title"), res.getString("Sec"), res.getString("Semester"), res.getString("Year"), res.getString("Enrollment"));
					}
				}
				success = true;
			}
			catch (Exception exc)
			{}
		}
	}
	*/
}
