public class ShiftRotateInstructions {
    //Shift Register by Count
	public void Op31(GeneralRegister R, int r, int count, int lr, int al) {
		if (count == 0) return;
		int value = R.getValue(r);
		if (al == 0) { // Arithmetic shift
			if (lr == 1) { // Left
				int shifted = (value << count) & 0xFFFF;
				R.setValue(r, shifted);
			} else { // Right
				int shifted = (value >>> count) & 0xFFFF;
				R.setValue(r, shifted);
			}
		} else { // Logical shift
			if (lr == 1) { // Left
				int shifted = (value << count) & 0xFFFF;
				R.setValue(r, shifted);
			} else { // Right
				int shifted = (value >>> count) & 0xFFFF;
				R.setValue(r, shifted);
			}
		}
	}
    
	// Rotate Register by Count
	public void Op32(GeneralRegister R, int r, int count, int lr, int al) {
		if (count == 0) return;
		if (al != 1) return; // Only logical rotates supported
		int value = R.getValue(r) & 0xFFFF;
		count = count % 16;
		if (lr == 1) { // Left rotate
			int rotated = ((value << count) | (value >>> (16 - count))) & 0xFFFF;
			R.setValue(r, rotated);
		} else { // Right rotate
			int rotated = ((value >>> count) | (value << (16 - count))) & 0xFFFF;
			R.setValue(r, rotated);
		}
	}
}
