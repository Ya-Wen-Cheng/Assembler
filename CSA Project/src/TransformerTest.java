import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TransformerTest {
	Transformer tf;

	@BeforeEach
	void setUp() throws Exception {
		tf = new Transformer();
	}

	@AfterEach
	void tearDown() throws Exception {
	}

	@Test
	void testDecimalToBinary() {
		char[] data = tf.DecimalToBinary((short)31, "12345".toCharArray(), 5);
		assertArrayEquals(data, "11111".toCharArray());
		char[] data1 = tf.DecimalToBinary((short)255, "12345678".toCharArray(), 8);
		assertArrayEquals(data1, "11111111".toCharArray());
	}

	@Test
	void testBinaryToDecimal() {
		short data = tf.BinaryToDecimal("11111".toCharArray(), 5);
		assertEquals(data, (short)31);
		short data1 = tf.BinaryToDecimal("11111111".toCharArray(), 8);
		assertEquals(data1, (short)255);
	}

	@Test
	void testOctToDecimal() {
		short data = tf.OctToDecimal("157");
		assertEquals(data, (short)111);
		short data1 = tf.OctToDecimal("24");
		assertEquals(data1, (short)20);
	}

	@Test
	void testSrcToDes() {
		fail("Not yet implemented");
	}

}
