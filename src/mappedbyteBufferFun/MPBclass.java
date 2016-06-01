package mappedbyteBufferFun;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MPBclass {
	static ByteArrayOutputStream bos = new ByteArrayOutputStream();
	static ObjectOutput out = null;
	
	static File database;
	static FileChannel databaseChannel;
	static MappedByteBuffer databaseMap;
	
	@SuppressWarnings("resource")
	public static void main(String[] args) throws IOException, ClassNotFoundException {
		// TODO Auto-generated method stub

		
		database = new File("datab1.binary");
		database.delete();
		databaseChannel = new RandomAccessFile(database, "rw").getChannel();
		databaseMap = databaseChannel.map(FileChannel.MapMode.READ_WRITE, 0,DataBase.DEFAULT_DATABASE_SIZE * DataBase.DEFAULT_BUFFER_SIZE );
		databaseMap.rewind();
		
	//	BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
	//	String inputString;
		
		
		DataBase db = new DataBase("datab1.binary");
		Account acc = new Account(db.generateID() ,"1", "1", "1234567890", "ul. hujovaja 15");
		Account acc2 = new Account(db.generateID() ,"2", "2", "1234567891", "ul. hujovaja 16");
		Account acc3 = new Account(db.generateID() ,"3", "3", "1234567892", "ul. hujovaja 17");
		acc2.setBalance(100f+acc2.getBalance());
		db.createUser(acc);
		
		db.createUser(acc);
		db.createUser(acc);
		db.createUser(acc2);
		db.createUser(acc3);
		//Arrays.fill(accountBuffer,(byte) 0);
		///db.getUserByteBuffer(2).put(accountBuffer);
		db.deleteUser(3);
		db.createUser(acc3);
		db.deposit(1,1000f);
		db.withdraw(1,1000f);
		db.transfer(2,1,100f);
		
		Pattern pat = Pattern.compile(".*1.*");
		Matcher mat = pat.matcher("aaaaab");
		System.out.println(mat.matches());
		
		List<Account> x= db.getAllUsers();
		for(Account i : x) { 
			System.out.println(i.toString());
		}
		System.exit(0);
	}
	
	public static byte[] serialize(Object obj) throws IOException {
	    ByteArrayOutputStream out = new ByteArrayOutputStream();
	    ObjectOutputStream os = new ObjectOutputStream(out);
	    os.writeObject(obj);
	    return out.toByteArray();
	}
	public static Object deserialize(byte[] data) throws IOException, ClassNotFoundException {
	    ByteArrayInputStream in = new ByteArrayInputStream(data);
	    ObjectInputStream is = new ObjectInputStream(in);
	    return is.readObject();
	}
}
