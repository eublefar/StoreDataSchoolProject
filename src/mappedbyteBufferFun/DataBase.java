package mappedbyteBufferFun;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.io.StreamCorruptedException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.regex.Pattern;

public class DataBase {

	private File database;
	private FileChannel databaseChannel;
	public MappedByteBuffer databaseMap;
	private int currentID;
	private int dataBaseSize;
	private int recordSize;

	static public int DEFAULT_BUFFER_SIZE = 300;
	static public int DEFAULT_DATABASE_SIZE = 100;

	public DataBase(String databaseName, int dbSize, int accBufferSize) throws IOException {
		recordSize = accBufferSize;
		currentID = 0;
		dataBaseSize = dbSize;
		database = new File(databaseName);
		try {
			databaseChannel = new RandomAccessFile(database, "rw").getChannel();
			if (database.exists()) {
				databaseMap = databaseChannel.map(FileChannel.MapMode.READ_WRITE, 4, dataBaseSize * recordSize);
				currentID = databaseMap.getInt();
			} else {
				databaseMap = databaseChannel.map(FileChannel.MapMode.READ_WRITE, 0, dataBaseSize * recordSize);
				databaseMap.putInt(currentID);
				databaseMap = databaseChannel.map(FileChannel.MapMode.READ_WRITE, 4, dataBaseSize * recordSize);
			}
		} catch (IOException e) {
			System.err.println("cant open file or create: " + databaseName);
			databaseChannel.close();
			e.printStackTrace();
		}
		databaseMap.flip();
	}

	public DataBase(String databaseName) throws IOException {
		recordSize = DEFAULT_BUFFER_SIZE;
		dataBaseSize = DEFAULT_DATABASE_SIZE;
		database = new File(databaseName);
		try {
			databaseChannel = new RandomAccessFile(database, "rw").getChannel();
			databaseMap = databaseChannel.map(FileChannel.MapMode.READ_WRITE, 0, dataBaseSize * recordSize);
		} catch (IOException e) {
			System.err.println("cant open file or create: " + databaseName);
			databaseChannel.close();
			e.printStackTrace();
		}
		databaseMap.flip();
	}

	public void createUser(Account user) throws IOException {
		byte[] accountBuffer = new byte[recordSize];
		findFirstEmpty();
		accountBuffer = Arrays.copyOf(serialize(user), recordSize);
		databaseMap.put(accountBuffer);
	}

	public void deleteUser(int id) {
		byte[] accountBuffer = new byte[recordSize];
		Arrays.fill(accountBuffer, (byte) 0);
		getUserByteBuffer(id).put(accountBuffer);

	}

	public void deposit(int id, float amount) throws ClassNotFoundException, IOException {
		byte[] accountBuffer = new byte[recordSize];
		ByteBuffer account = getUserByteBuffer(id).get(accountBuffer);
		Account temp = (Account) deserialize(accountBuffer);
		temp.setBalance(temp.getBalance() + amount);
		account.rewind();
		account.put(serialize(temp));
	}

	public void withdraw(int id, float amount) throws ClassNotFoundException, IOException {
		byte[] accountBuffer = new byte[recordSize];
		ByteBuffer account = getUserByteBuffer(id).get(accountBuffer);
		Account temp = (Account) deserialize(accountBuffer);
		temp.setBalance(temp.getBalance() - amount);
		account.rewind();
		account.put(serialize(temp));
	}

	public void transfer(int from, int to, float amount) throws IOException, ClassNotFoundException {
		byte[] accountToBuffer = new byte[recordSize];
		byte[] accountFromBuffer = new byte[recordSize];

		ByteBuffer accountFrom = getUserByteBuffer(from).get(accountFromBuffer);
		ByteBuffer accountTo = getUserByteBuffer(to).get(accountToBuffer);

		Account tempFrom = (Account) deserialize(accountFromBuffer);
		Account tempTo = (Account) deserialize(accountToBuffer);

		if (tempFrom.getBalance() < amount) {
			throw new IOException();
		}

		tempFrom.setBalance(tempFrom.getBalance() - amount);
		tempTo.setBalance(tempTo.getBalance() + amount);

		accountFrom.rewind();
		accountTo.rewind();
		accountFrom.put(serialize(tempFrom));
		accountTo.put(serialize(tempTo));
	}

	public List<Account> getAllUsers() throws ClassNotFoundException, IOException {
		byte[] accountBuffer = new byte[recordSize];
		List<Account> res = new ArrayList<Account>();
		databaseMap.rewind();
		while (databaseMap.hasRemaining()) {
			databaseMap.get(accountBuffer);
			try {
				res.add((Account) deserialize(accountBuffer));
			} catch (StreamCorruptedException e) {

			}
		}
		return res;
	}

	// TODO: Create all the filters ffs
	public List<Account> getUsersByName(Pattern namePattern) throws ClassNotFoundException, IOException {
		byte[] accountBuffer = new byte[recordSize];
		List<Account> res = new ArrayList<Account>();
		databaseMap.rewind();
		while (databaseMap.hasRemaining()) {
			databaseMap.get(accountBuffer);
			try {
				Account acc = (Account) deserialize(accountBuffer);
				if (namePattern.matcher(acc.getName().split(" ")[0]).matches()) {
					res.add(acc);
				}

			} catch (StreamCorruptedException e) {

			}

		}
		return res;
	}

	public List<Account> getUsersBySurname(Pattern surnamePattern) throws ClassNotFoundException, IOException {
		byte[] accountBuffer = new byte[recordSize];
		List<Account> res = new ArrayList<Account>();
		databaseMap.rewind();
		while (databaseMap.hasRemaining()) {
			databaseMap.get(accountBuffer);
			try {
				Account acc = (Account) deserialize(accountBuffer);
				if (surnamePattern.matcher(acc.getName().split(" ")[1]).matches()) {
					res.add(acc);
				}

			} catch (StreamCorruptedException e) {

			}

		}
		return res;
	}

	public List<Account> getUsersByAddress(Pattern addressPattern) throws ClassNotFoundException, IOException {
		byte[] accountBuffer = new byte[recordSize];
		List<Account> res = new ArrayList<Account>();
		databaseMap.rewind();
		while (databaseMap.hasRemaining()) {
			databaseMap.get(accountBuffer);
			try {
				Account acc = (Account) deserialize(accountBuffer);
				if (addressPattern.matcher(acc.getAddress()).matches()) {
					res.add(acc);
				}
			} catch (StreamCorruptedException e) {

			}

		}
		return res;
	}

	public List<Account> getUsersByPesel(Pattern peselPattern) throws ClassNotFoundException, IOException {
		byte[] accountBuffer = new byte[recordSize];
		List<Account> res = new ArrayList<Account>();
		databaseMap.rewind();
		while (databaseMap.hasRemaining()) {
			databaseMap.get(accountBuffer);
			try {
				Account acc = (Account) deserialize(accountBuffer);
				if (peselPattern.matcher(acc.getPesel()).matches()) {
					res.add(acc);
				}
			} catch (StreamCorruptedException e) {

			}

		}
		return res;
	}

	public List<Account> getUsersByBalance(float compared, BiFunction<Float, Float, Boolean> fn)
			throws ClassNotFoundException, IOException {
		byte[] accountBuffer = new byte[recordSize];
		List<Account> res = new ArrayList<Account>();
		databaseMap.rewind();
		while (databaseMap.hasRemaining()) {
			databaseMap.get(accountBuffer);
			try {
				Account acc = (Account) deserialize(accountBuffer);
				if (fn.apply(acc.getBalance(), compared)) {
					res.add(acc);
				}
			} catch (StreamCorruptedException e) {

			}

		}
		return res;
	}

	private void increaseDBSize(int records) throws IOException {

		dataBaseSize += records;
		databaseMap = databaseChannel.map(FileChannel.MapMode.READ_WRITE, 0, dataBaseSize * recordSize);
	}

	public ByteBuffer getUserByteBuffer(int id) {
		byte[] accountBuffer = new byte[recordSize];
		databaseMap.rewind();
		while (databaseMap.hasRemaining()) {
			databaseMap.get(accountBuffer);
			Account acc;
			try {
				acc = (Account) deserialize(accountBuffer);
				if (acc.getId() == id) {
					databaseMap.position(databaseMap.position() - recordSize);
					ByteBuffer temp = databaseMap.slice();
					return temp;
				}

			} catch (StreamCorruptedException e) {

			} catch (IOException | ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return null;
	}

	private void findFirstEmpty() throws IOException {
		byte[] accountBuffer = new byte[recordSize];
		databaseMap.rewind();
		while (databaseMap.hasRemaining()) {

			databaseMap.get(accountBuffer);

			try {
				deserialize(accountBuffer);
			} catch (StreamCorruptedException e) {

				databaseMap.position(databaseMap.position() - accountBuffer.length);
				return;

			} catch (IOException | ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (databaseMap.capacity() == databaseMap.limit()) {
			increaseDBSize(dataBaseSize / 10);

		} else {
			databaseMap.position(databaseMap.limit());
			databaseMap.limit(databaseMap.position() + recordSize);
		}
	}

	public int generateID() {
		return ++currentID;
	}

	private static byte[] serialize(Object obj) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ObjectOutputStream os = new ObjectOutputStream(out);
		os.writeObject(obj);
		return out.toByteArray();
	}

	private static Object deserialize(byte[] data) throws IOException, ClassNotFoundException {
		ByteArrayInputStream in = new ByteArrayInputStream(data);
		ObjectInputStream is = new ObjectInputStream(in);
		return is.readObject();
	}
}
