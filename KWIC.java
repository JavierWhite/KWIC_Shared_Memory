import java.io.File;                  
import java.io.FileNotFoundException; 
import java.util.Scanner;

public class KWIC {
	private char[] chars_ = new char[5000];
    private char[] shift_chars_ = new char[5000];
	private int[] line_index_ = new int [100];
	private int lineCount = 0;
	private int charCount = 0;
	private int shiftCount = 0;
	private int shiftCharCount = 0;
	private int alpha_index_[] = new int [100];
	

	public static void usage() {

		System.out.println("Usage:");
		System.out.println("  java KWIC <filename>");
		System.out.println();
		System.out.println("Example:");
		System.out.println("  java KWIC input.txt");

	}

	public void input(String file) {
		line_index_[0] = 0;

		try {
			Scanner sc = new Scanner(new File(file));

			while(sc.hasNextLine()) {
				String line = sc.nextLine();

				for(int i = 0; i < line.length(); i++) {
					chars_[charCount] = line.charAt(i);
					charCount++;
				}
				lineCount++;
				line_index_[lineCount] = charCount;
			}
			sc.close();
		} catch(FileNotFoundException e) {
			System.err.println("file not found");
			System.exit(1);
		}
	}

    public void circularShift() {
        try {

          if (lineCount == 0) {
            throw new IllegalStateException("no input data");
          }

          int[] original_index = new int[lineCount + 1];

          for (int i = 0; i <= lineCount; i++) {
            original_index[i] = line_index_[i];
          }

          shiftCount = 0;
          shiftCharCount = 0;

          for (int line = 0; line < lineCount; line++) {
            int lineStart = original_index[line];
            int lineEnd = original_index[line + 1];

            if (lineStart < 0 || lineEnd > charCount || lineStart > lineEnd) {
              throw new IllegalStateException("invalid line index");
            }

            int[] wordStart = new int[100];

            int[] wordEnd = new int[100];

            int wordCount = 0;

            int position = lineStart;

            while (position < lineEnd) {

              while (position < lineEnd && chars_[position] == ' ') {
                position++;
              }

              if (position >= lineEnd) {
                break;
              }

              if (wordCount >= wordStart.length) {
                throw new IllegalStateException("too many words in line");
              }

              wordStart[wordCount] = position;

              while (position < lineEnd && chars_[position] != ' ') {
                position++;

              }

              wordEnd[wordCount] = position;
              wordCount++;
            }

            for (int shift = 0; shift < wordCount; shift++) {

              if (shiftCount >= line_index_.length - 1) {
                throw new IllegalStateException("too many circular shifts");
              }

              line_index_[shiftCount] = shiftCharCount;

              for (int i = 0; i < wordCount; i++) {

                int word = (shift + i) % wordCount;

                if (i > 0) {

                  if (shiftCharCount >= shift_chars_.length) {
                    throw new IllegalStateException(
                                                              "shift array is full"
                                                            );

                  }

                  shift_chars_[shiftCharCount] = ' ';
                  shiftCharCount++;
                }

                for (int j = wordStart[word]; j < wordEnd[word]; j++) {

                  if (shiftCharCount >= shift_chars_.length) {
                    throw new IllegalStateException("shift array is full");
                  }
                  shift_chars_[shiftCharCount] = chars_[j];
                  shiftCharCount++;

                }

              }

              shiftCount++;
            }
          }

          line_index_[shiftCount] = shiftCharCount;

        } catch (ArrayIndexOutOfBoundsException e) {
          System.err.println("circular shift error: array size exceeded");
          System.exit(1);

        } catch (IllegalStateException e) {
          System.err.println("circular shift error: " + e.getMessage());
          System.exit(1);

        } catch (Exception e) {
          System.err.println(
                                "unexpected circular shift error"
                              );
          System.exit(1);
        }
    }

	public int compareShifts(int shift1, int shift2) {
		int shift1Pos = line_index_[shift1];
		int shift1End = line_index_[shift1 + 1];
		int shift2Pos= line_index_[shift2];
		int shift2End = line_index_[shift2 + 1];

		while(shift1Pos < shift1End && shift2Pos < shift2End) {
			if (shift_chars_[shift1Pos] < shift_chars_[shift2Pos]) {
				return -1; // shift1 < shift2
			}

			if (shift_chars_[shift1Pos] > shift_chars_[shift2Pos]) {
				return 1; // shift1 > shift2
			}

			shift1Pos++;
			shift2Pos++;
		}

		if (shift1Pos == shift1End && shift2Pos < shift2End) {
			return -1; // shift1 < shift2
		}

		if (shift2Pos == shift2End && shift1Pos < shift1End) {
			return 1; // shift1 > shift2
		}

		return 0; // shift1 == shift2
	}

	public void alphabetizing() {
		for(int i = 0; i < shiftCount; i++) {
			alpha_index_[i] = i;
		}

		for(int i = 1; i < shiftCount; i++) {
			int current = alpha_index_[i];
			int j = i - 1;

			while(j >= 0 && compareShifts(alpha_index_[j], current) > 0) {
				alpha_index_[j + 1] = alpha_index_[j];
				j--;
			}
			alpha_index_[j + 1] = current;
		}
	}

	public void output() {
		System.out.println("KWIC Output");
		System.out.println("-------------------");

		for (int i = 0; i < shiftCount; i++){
			int shift = alpha_index_[i];
            int start = line_index_[shift];
			int end = line_index_[shift + 1];

			for (int j = start; j < end; j++){
				System.out.print(shift_chars_[j]);
			}

			System.out.println();
		}

	}
	
	public static void main(String[] args) {
		KWIC kwic = new KWIC();

        if (args.length != 1) {
			System.err.println("specify filename");
			usage();
			System.exit(1);
        }
		
		kwic.input(args[0]);
        kwic.circularShift();
        kwic.alphabetizing();
        kwic.output();
	}
}
