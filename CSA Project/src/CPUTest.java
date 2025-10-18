import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CPUTest {
	CPU cpu;

	@BeforeEach
	void setUp() throws Exception {
		cpu = new CPU();
	}

	@AfterEach
	void tearDown() throws Exception {
		cpu = null;
	}


	@Test
	void testReset() {
		fail("Not yet implemented");
	}

	@Test
	void testSetGPR() {
		cpu.setGPR((short)1, (short)31);
		System.out.print(cpu.getGPR((short)1));
		assertArrayEquals(cpu.getGPR((short)1), "           11111".toCharArray());
		cpu.setGPR((short)2, (short)255);
		assertEquals(cpu.getGPR((short)2), "        11111111".toCharArray());
	}

	@Test
	void testSetIXR() {
		cpu.setIXR((short)1, (short)1234);
		assertEquals(cpu.getIXR((short)1), (short)1234);
		cpu.setIXR((short)2, (short)5678);
		assertEquals(cpu.getIXR((short)2), (short)5678);
		cpu.setIXR((short)3, (short)9101);
		assertEquals(cpu.getIXR((short)3), (short)9101);
	}

	@Test
	void testSetProgramCounter() {
		cpu.setProgramCounter((short)31);
		assertArrayEquals(cpu.getProgramCounter(), "11111".toCharArray());
		cpu.setProgramCounter((short)255);
		assertArrayEquals(cpu.getProgramCounter(), "11111111".toCharArray());
	}

	@Test
	void testSetMemoryAddressRegister() {
		cpu.setMemoryAddressRegister((short)31);
		assertArrayEquals(cpu.getMemoryAddressRegister(), "11111".toCharArray());
		cpu.setProgramCounter((short)255);
		assertArrayEquals(cpu.getMemoryAddressRegister(), "11111111".toCharArray());
	}

	@Test
	void testSetMemoryBufferRegister() {
		cpu.setMemoryBufferRegister((short)31);
		assertArrayEquals(cpu.getMemoryBufferRegister(), "11111".toCharArray());
		cpu.setMemoryBufferRegister((short)255);
		assertArrayEquals(cpu.getMemoryBufferRegister(), "11111111".toCharArray());
	}

	@Test
	void testGetMemoryAddressValue() {
		fail("Not yet implemented");
	}

	@Test
	void testGetMemoryBufferValue() {
		fail("Not yet implemented");
	}

	@Test
	void testExecute() {
		fail("Not yet implemented");
	}

}
