package mappedbyteBufferFun;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

public class Resolver {

	static DataBase dataBase;

	// TODO: handle AALLLLL EXCEPTIONS
	public static void resolve(String input) throws IOException, NumberFormatException, ClassNotFoundException {
		String[] inputWords = input.split("[ ,,]");

		// for(Object it : inputWords) {
		switch (inputWords[0]) {

		case "withdraw": {
			handleWithdraw(java.util.Arrays.copyOfRange(inputWords, 1, inputWords.length));
			break;
		}
		case "deposit": {
			handleDeposit(java.util.Arrays.copyOfRange(inputWords, 1, inputWords.length));
			break;
		}
		case "transfer": {
			dataBase.transfer(Integer.parseInt(inputWords[1]), Integer.parseInt(inputWords[2]), Float.parseFloat(inputWords[3]));
			break;
		}
		case "create": {
			handleCreate(java.util.Arrays.copyOfRange(inputWords, 1, inputWords.length));
			break;
		}
		case "delete": {
			handleDelete(java.util.Arrays.copyOfRange(inputWords, 1, inputWords.length));
			break;
		}
		case "get": {
			handleGet(java.util.Arrays.copyOfRange(inputWords, 1, inputWords.length));
			break;
		}
		case "help": {
			System.out.println("create user 'name' 'surname' 'pesel' 'address'\t - create user\n"
					+ "delete user 'id'\t\t\t\t - delete user\n"
					+ "withdraw 'id' 'amount' \t\t\t\t - withdraw 'amount' from account with 'id'\n"
					+ "deposit 'id' 'amount'  \t\t\t\t - deposit 'amount' to account with 'id'\n"
					+ "get user ['field'='regular expression']\t\t - get accounts from database\n");
			break;
		}
		}
	}

	private static void handleCreate(String[] input) throws IOException {
		if(input.length!=5) {
			System.err.println("Wrong number of parameters");
		} else if(input[3].length()!=11) {
			System.err.println("Pesel should have 11 chars");
		}else if (input[0].equals("user")) {
			dataBase.createUser(new Account(dataBase.generateID(), input[1], input[2], input[3], input[4]));
		} else {
			System.err.println("Wrong or no object to create");
		}
	}

	private static void handleDeposit(String[] input)
			throws NumberFormatException, ClassNotFoundException, IOException {
		dataBase.deposit(Integer.parseInt(input[0]), Float.parseFloat(input[1]));
		System.out.println("Deposit success");
	}

	private static void handleWithdraw(String[] input)
			throws NumberFormatException, ClassNotFoundException, IOException {
		dataBase.withdraw(Integer.parseInt(input[0]), Float.parseFloat(input[1]));
		System.out.println("Withdraw success");
	}

	private static void handleDelete(String[] input) throws IOException {
		if (input[0].equals("user")) {
			dataBase.deleteUser(Integer.parseInt(input[1]));
		} else {
			System.err.println("No object to delete");
		}
			
	}

	private static void handleGet(String[] input) throws IOException, ClassNotFoundException {
		if (input.length == 0) {
			System.err.println("No object to get");
		} else if (input[0].equals("user")) {
			if (input.length == 1) {
				List<Account> x = dataBase.getAllUsers();
				for (Account i : x) {
					System.out.println(i.toString());
				}
			} else {
				List<Account> results = new ArrayList<Account>();
				for (String it : java.util.Arrays.copyOfRange(input, 1, input.length)) {
					it.replace(",", "");
					if (!it.contains("balance")) {
						String[] filter = it.split("=");
						results.addAll(resolveFilter(filter));
					} else {

					}
				}
				for (Account i : results) {
					System.out.println(i.toString());
				}
			}

		}
	}

	private static List<Account> resolveFilter(String[] input) throws ClassNotFoundException, IOException {
		List<Account> res = null;
		Pattern p = Pattern.compile(input[1]);
		switch (input[0]) {
		case "name": {
			res = dataBase.getUsersByName(p);
			break;
		}
		case "surname": {
			res = dataBase.getUsersBySurname(p);
			break;
		}
		case "address": {
			res = dataBase.getUsersByAddress(p);
			break;
		}
		case "pesel": {
			res = dataBase.getUsersByPesel(p);
			break;
		}
		default: {
			System.err.println("No such field");
			break;
		}
		}
		return res;
	}

	public static void main(String[] args) throws NumberFormatException, ClassNotFoundException, IOException {
		// TODO Auto-generated method stub
		//File db = new File("new.db");
		//db.delete();
		dataBase = new DataBase("new.db");
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("Bank Management program \nAuthor: Mykyta Makarov\n\n");
		if(args.length==0) {
			System.out.print("\n#>");
			while (true) {

				Resolver.resolve(br.readLine());
				System.out.print("\n#>");

			/*
			 * List<Account> x= dataBase.getAllUsers(); for(Account i : x) {
			 * System.out.println(i.toString()); }
			 */
			}
		} else {
			if(Pattern.compile(".*[.]dbscr[.]txt").matcher( args[0]).matches()) {
				File script = new File(args[0]);
				if(script.getAbsoluteFile().exists()) {
					Scanner sc = new Scanner(script.getAbsoluteFile());
					while (sc.hasNext()){
						String a = sc.nextLine();
						System.out.println(a);
						Resolver.resolve(a);
					}
				} else System.out.println("no script file with such name : " + script.getAbsolutePath());
			}else System.out.println("no script file with such name ");
		}
	}

}
