/*
	- add exp_date to affiliation
	- figure out the deal with total_miles
	- If you wanna go in at the end and add more data be my guest
*/

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.UUID;

public class datagenerating
{
	public static void main(String[] args) 
	{
		//custData(100);
		//locData(25);
		vehicleAndParkData(250);

	}

	public static void custData (int rows)
	{
		String ins = "Insert into ";
		String vals = " values (";
		String closeVals = ");";
		String temp = "TEMPLATE";
		String table = "customer (license, name, address, state)";

		List<String> states = Arrays.asList("AK", "AL", "AR", "AZ", "CA", "CO", "CT", "DC", "DE", "FL", "GA", "HI", "IA", "ID", "IL", "IN", "KS", "KY", "LA", "MA", "MD", "ME", "MI", "MN", "MO", "MS", "MT", "NC", "ND", "NE", "NH", "NJ", "NM", "NV", "NY", "OH", "OK", "OR", "PA", "RI", "SC", "SD", "TN", "TX", "UT", "VA", "VT", "WA", "WI", "WV", "WY");


		for (int i = 0; i < rows; i++) 
		{
			String rand_license = "" + rand_int(8);
			String rand_state = rand_string(states, false);


			String line = ins + table + vals + add(rand_license) + add(temp) + add(temp) + addEnd(rand_state) + closeVals;
			System.out.println(line);
		}
	}

	public static void locData (int rows)
	{
		String ins = "Insert into ";
		String vals = " values (";
		String closeVals = ");";
		String temp = "TEMPLATE";
		String table = "location (address, state, loc_type, inventory)";

		List<String> states = Arrays.asList("AK", "AL", "AR", "AZ", "CA", "CO", "CT", "DC", "DE", "FL", "GA", "HI", "IA", "ID", "IL", "IN", "KS", "KY", "LA", "MA", "MD", "ME", "MI", "MN", "MO", "MS", "MT", "NC", "ND", "NE", "NH", "NJ", "NM", "NV", "NY", "OH", "OK", "OR", "PA", "RI", "SC", "SD", "TN", "TX", "UT", "VA", "VT", "WA", "WI", "WV", "WY");
		List<String> loc_types = Arrays.asList("Airport","Train Station","City Center", "Junkyard");

		for (int i = 0; i < rows; i++)
		{
			String rand_state = rand_string(states,false);
			String rand_loc_type = rand_string(loc_types, false);

			String line = ins + table + vals + add(temp) + add(rand_state) + add(rand_loc_type) + addEnd(0) + closeVals;
			System.out.println(line);
		}
	}

	public static void vehicleAndParkData (int rows)
	{
		String ins = "Insert into ";
		String vals = " values (";
		String closeVals = ");";
		String temp = "TEMPLATE";
		String table1 = "vehicle (license_plate, make, model, year, vehicle_type, baseRate, odometer, fuel_used, damage)";
		String table2 = "parks (loc_ID, license_plate)";

		List<String> models = Arrays.asList("Axiom","Deux Chevaux (2CV)","Gremlin","Hornet","Pacer","Edsel","","Vega","Yugo","Trabant","Prowler","Aztek","Pinto","Toyopet","Lada","Thing","Leaflet","Bongo","Amazon","Wrangler");

		for (int i = 0; i < rows; i++)
		{
			String veh_license = rand_alphaNum(8);
			String rand_model = rand_string(models, false);
			String make = getMake(rand_model);
			String year = "" + getRandYear(rand_model);
			String type = getType(rand_model);
			String baserate = "" + getRate(rand_model);


			String vehLine = ins + table1 + vals + add(veh_license) + add(make) + add(rand_model) + add(year) + add(type) + add(baserate) + add(0) + add(0) + addEnd("no") + closeVals;
			System.out.println(vehLine);

			int rand_loc = rand_btwn(1,25);

			String parkLine = ins + table2 + vals + add(rand_loc) + addEnd(veh_license) + closeVals;
			System.out.println(parkLine);
		}
	}

	/*
	public static void rentalData (int rows)
	{

	-- helpful shit I guess

		List<String> businesses = Arrays.asList("Google","Microsoft","Apple","Samsung","IBM","Hewlett-Packard","Dell","Sony");
		List<String> membership = Arrays.asList("AAA","AARP");

		int rand_ccNopt1 = rand_int(8);
		int rand_ccNopt2 = rand_int(8);
		String ccNo = "" + rand_ccNopt1 + rand_ccNopt2;
		Double discount = 0.0;
		String rand_bus = rand_string(businesses, true);
		String rand_mem = rand_string(membership, true);
		if (rand_mem != "NULL")
		{
			discount = 0.25;
		}
		else if (rand_bus != "NULL")
		{
			discount = 0.20;
		}

		+ add(ccNo) + add(discount) + add(rand_bus) + addEnd(rand_mem) 

	}

	*/

	/* Helper functions */

	public static String add (String input)
	{
		if (input == "NULL")
		{
			return input + ", ";
		}

		return "'" + input + "', ";
	}

	public static String add (int input)
	{
		return input + ", ";
	}

	public static String add (double input)
	{
		return input + ", ";
	}
	
	public static String addEnd (String input)
	{
		if (input == "NULL")
		{
			return input;
		}

		return "'" + input + "'";
	}

	public static String addEnd (int input)
	{
		return "" + input;
	}

	public static String rand_string (List<String> list, boolean nullable)
	{
		Random rand = new Random();

		if (nullable && (ThreadLocalRandom.current().nextInt(1,11)) > 3) // make it slightly likely to have discount
		{
			return "NULL";
		}

		String rand_str = list.get(rand.nextInt(list.size()));
		return rand_str;
	}

	public static int rand_int (int digits)
	{
		Random rand = new Random();

		int i = (int) ((Math.pow(10,digits))/10);
		return i + rand.nextInt((int)(i*9));
	}

	public static String getType (String model)
	{
		String type = "";
		String[][] types = new String[][] 
		{
			{"SUV","Axiom","Wrangler"},
			{"minicar","Deux Chevaux (2CV)"},
			{"subcompact","Gremlin","Yugo","Trabant","Pinto","Toyopet"},
			{"compact","Hornet","Pacer","Vega","Lada","Bongo"},
			{"full size","Edsel","Leaflet"},
			{"sports car","Prowler"},
			{"minivan","Aztek"},
			{"truck","Thing","Amazon"}
		};

		for (int i = 0; i < types.length; i++)
		{
			for (int j = 0; j < types[i].length; j++) 
			{
				if (types[i][j] == model)
				{
					type = types[i][0];
				}
			}
		}
		
		return type;
	}

	public static String getMake (String model)
	{
		String type = "";
		String[][] types = new String[][] 
		{
			{"Isuzu","Axiom","Amazon"},
			{"Citroen","Deux Chevaux (2CV)"},
			{"AMC","Gremlin","Hornet","Pacer"},
			{"Ford","Edsel","Pinto"},
			{"Chevrolet","Vega","Leaflet"},
			{"Yugo","Yugo","Bongo"},
			{"Trabant","Trabant"},
			{"Plymouth","Prowler"},
			{"Pontiac","Aztek"},
			{"Toyota","Toyopet"},
			{"VAZ","Lada","Wrangler"},
			{"Volkswagen","Thing"}
		};

		for (int i = 0; i < types.length; i++)
		{
			for (int j = 0; j < types[i].length; j++) 
			{
				if (types[i][j] == model)
				{
					type = types[i][0];
				}
			}
		}
		
		return type;		
	}

	public static int getRandYear (String model)
	{
		int minYear = 0;
		int maxYear = 0;
		int randYear = 0;

		switch (model) 
		{
			case "Axiom":
				minYear = 2001;
				maxYear = 2001;
				break;
			case "Deux Chevaux (2CV)":
				minYear = 1948;
				maxYear = 1990;
				break;
			case "Gremlin":
				minYear = 1971;
				maxYear = 1978;
				break;
			case "Hornet":
				minYear = 1969;
				maxYear = 1970;
				break;
			case "Pacer":
				minYear = 1975;
				maxYear = 1979;
				break;
			case "Edsel":
				minYear = 1958;
				maxYear = 1960;
				break;
			case "Vega":
				minYear = 1971;
				maxYear = 1977;
				break;
			case "Yugo":
				minYear = 2007;
				maxYear = 2008;
				break;
			case "Trabant":
				minYear = 1964;
				maxYear = 1990;
				break;
			case "Prowler":
				minYear = 1999;
				maxYear = 2002;
				break;
			case "Aztek":
				minYear = 2001;
				maxYear = 2005;
				break;
			case "Pinto":
				minYear = 1971;
				maxYear = 1980;
				break;
			case "Toyopet":
				minYear = 1959;
				maxYear = 1961;
				break;
			case "Lada":
				minYear = 1970;
				maxYear = 1989;
				break;
			case "Thing":
				minYear = 1970;
				maxYear = 1980;
				break;
			case "Leaflet":
				minYear = 1988;
				maxYear = 1999;
			case "Bongo":
				minYear = 2010;
				maxYear = 2015;
			case "Amazon":
				minYear = 1988;
				maxYear = 2000;
			case "Wrangler":
				minYear = 2006;
				maxYear = 2011;
			default:
				randYear = 1;
				break;
		}

		randYear = ThreadLocalRandom.current().nextInt(minYear, maxYear + 1);
		return randYear;
	}

	public static int getRate (String model)
	{
		int rate = 0;

		switch (model) 
		{
			case "Axiom":
				rate = 70;
				break;
			case "Deux Chevaux (2CV)":
				rate = 80;
				break;
			case "Gremlin":
				rate = 65;
				break;
			case "Hornet":
				rate = 75;
				break;
			case "Pacer":
				rate = 70;
				break;
			case "Edsel":
				rate = 69;
				break;
			case "Vega":
				rate = 75;
				break;
			case "Yugo":
				rate = 64;
				break;
			case "Trabant":
				rate = 77;
				break;
			case "Prowler":
				rate = 88;
				break;
			case "Aztek":
				rate = 66;
				break;
			case "Pinto":
				rate = 82;
				break;
			case "Toyopet":
				rate = 84;
				break;
			case "Lada":
				rate = 79;
				break;
			case "Thing":
				rate = 74;
				break;
			case "Leaflet":
				rate = 83;
			case "Bongo":
				rate = 72;
			case "Amazon":
				rate = 79;
			case "Wrangler":
				rate = 88;
			default:
				rate = 80;
				break;
		}
		
		return rate;
	}

	public static String rand_alphaNum(int length)
	{
		String ugly = UUID.randomUUID().toString();
		String pretty = "";
		int count = 0;

		for (int i = 0; i < ugly.length(); i++)
		{
			if (ugly.charAt(i) != '-')
			{
				pretty += Character.toUpperCase(ugly.charAt(i));
				if (++count >= length)
				{
					return pretty;
				}
			}
		}

		return pretty;
	}

	public static int rand_btwn (int min, int max)
	{
		return ThreadLocalRandom.current().nextInt(min, max + 1);
	}
}



















