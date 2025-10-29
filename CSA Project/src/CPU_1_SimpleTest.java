import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CPU_1_SimpleTest {
	CPU_1_Simple cpu; 

	@BeforeEach
	void setUp() throws Exception {
		cpu = new CPU_1_Simple();
	}

	@AfterEach
	void tearDown() throws Exception {
	}

	@Test
	void testSetProgramCounter() {
		cpu.setProgramCounter((short)1);
		assertArrayEquals(cpu.programCounter, "000000000001".toCharArray());
		cpu.setProgramCounter((short)3);
		assertArrayEquals(cpu.programCounter, "000000000011".toCharArray());
	}

	@Test
	void testSetMemoryAddressRegister() {
		fail("Not yet implemented");
	}

	@Test
	void testSetMemoryBufferRegister() {
		fail("Not yet implemented");
	}

	@Test
	void testSetConditionCode() {
		fail("Not yet implemented");
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
	void testGetGPR() {
		fail("Not yet implemented");
	}

	@Test
	void testGetIXR() {
		fail("Not yet implemented");
	}

	@Test
	void testGetProgramCounter() {
		fail("Not yet implemented");
	}

	@Test
	void testGetConditionCode() {
		fail("Not yet implemented");
	}

	@Test
	void testGetMemoryFaultRegister() {
		fail("Not yet implemented");
	}

	@Test
	void testExecute() {
		fail("Not yet implemented");
	}

	@Test
	void testExecuteLDR() {
		fail("Not yet implemented");
	}

	@Test
	void testExecuteSTR() {
		fail("Not yet implemented");
	}

	@Test
	void testExecuteLDA() {
		fail("Not yet implemented");
	}

	@Test
	void testExecuteLDX() {
		fail("Not yet implemented");
	}

	@Test
	void testExecuteSTX() {
		fail("Not yet implemented");
	}

	@Test
	void testExecuteAMR() {
		fail("Not yet implemented");
	}

	@Test
	void testExecuteSMR() {
		fail("Not yet implemented");
	}

	@Test
	void testExecuteAIR() {
		fail("Not yet implemented");
	}

	@Test
	void testExecuteSIR() {
		fail("Not yet implemented");
	}

	@Test
	void testExecuteInstruction() {
		fail("Not yet implemented");
	}

}
