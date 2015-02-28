/**
 * This code was NOT written by me.
 */
import java.util.*;
import java.io.*;

/** 
 * A lexicon is a word list. This lexicon supports two totally
 * different and separate data structures for maintaining the words:
 * a dawg (directed acyclic word graph) and a set of other
 * words. Typically the dawg is used for a large list read from a 
 * file in binary format.  The other list is for words added 
 * piecemeal at runtime.
 *
 * The dawg idea comes from an article by Appel & Jacobson, CACM May 1988.
 * This lexicon implementation only has the code to load/search the dawg.
 * The dawg builder code is quite a bit more intricate, and not included here.
 *
 */
/*  Code written by Julie Zelenski. */
public class Lexicon {

    /**
     *  Each edge is represented by one 32-bit struct.  The 5 "letter" bits 
     * indicate the character on this transition (expressed as integer from 1
     * to 26), the  "accept" bit indicates if you accept after appending that char 
     * (current path forms word), and the "lastEdge" bit marks this as the last
     * edge in a sequence of childeren.  The bulk of the bits (24) are used for 
     * the index within the edge array for the children of this node. The 
     * children are laid out contiguously in alphabetical order.
     */
    private int edges[];

    /** the root of the tree */
    private int start;

    /** vector to hold words added by the user after loading the dawg. */
    private Vector<String> otherWords = new Vector<String>();

    public Lexicon() {
        this("lexicon.dat");    
    }

    /**
     * Loads a lexicon from the specified file.  The file must
     * have the DAWG format, or it will not work.
     */
    protected Lexicon(String fileName) {
        try {
            DataInputStream in = new DataInputStream(new FileInputStream(
                        fileName));

            // the file starts with a checksum
            int header = ('D' << 24) | ('A' << 16) | ('W' << 8) | 'G';
            int fileHeader = in.readInt();

            in.read(); // skip ':'
            start = 0;

            // the next part is the start index
            while (true) {
                int ch = (char) in.readUnsignedByte();
                if (ch == ':')
                    break;
                start = start * 10 + (ch - '0');
            }

            // the next part is the number of edges
            int numEdges = 0;
            while (true) {
                int ch = (char) in.readUnsignedByte();
                if (ch == ':')
                    break;
                numEdges = numEdges * 10 + (ch - '0');
            }
            numEdges /= 4;

            // fail if header info is not well-formed.
            if (header != fileHeader || numEdges < 0 || start < 0) {
                throw new RuntimeException("Improperly formed lexicon file "
                    + fileName);
            }

            // read in all the edges.  Assumes Java ints are stored 
            // high-byte first
            edges = new int[numEdges];
            for (int i = 0; i < numEdges; i++) {
                long b0 = in.readUnsignedByte();
                long b1 = in.readUnsignedByte();
                long b2 = in.readUnsignedByte();
                long b3 = in.readUnsignedByte();
                edges[i] = (int) (b3 | (b2 << 8) | (b1 << 16) | (b0 << 24));
            }
            in.close();
        } catch (Exception e) {
            System.out.println(e);
            System.exit(0);
        }
    }

    /**
     * Add a word to the lexicon. 
     */
    public void add(String word) {
        word = word.toLowerCase();
        if (!otherWords.contains(word)) {
            otherWords.add(word);
        }
    }

    /**
     * Returns true if the word is contained in the lexicon.
     */
    public boolean contains(String word) {
        word = word.toLowerCase();
        int lastEdge = traceToLastEdge(word);
        if (lastEdge != -1 && edgeAccept(edges[lastEdge]))
            return true;
        return otherWords.contains(word);
    }

    /** 
     * This method returns true if any words in the lexicon begin with the
     * specified prefix, false otherwise. A word is defined to be a prefix of
     * itself and the empty string is a prefix of everything.
     */
    public boolean containsPrefix(String prefix) {
        prefix = prefix.toLowerCase();
        if (prefix.length() == 0)
            return true;
        if (traceToLastEdge(prefix) != -1)
            return true;
        for (int i = 0; i < otherWords.size(); i++) {
            if (otherWords.get(i).startsWith(prefix))
                return true;
        }
        return false;
    }

    /* 
     * Iterate over sequence of children to find one that
     * matches the given char.  returns -1 if we get to
     * last child without finding a match (thus no such
     * child edge exists).
     */
    private int findEdgeForChar(int childrenIndex, char ch) {
        int current = childrenIndex;
        while (true) {
            if (edgeLetter(edges[current]) == ch)
                return current;
            if (edgeLastEdge(edges[current]))
                return -1;
            current++;
        }
    }

    /* 
     * Given a string, trace out path through
     * dawg edge-by-edge. If path exists, return last edge
     * else returns -1.
     */
    private int traceToLastEdge(String s) {
        int current = findEdgeForChar(start, s.charAt(0));
        for (int i = 1; i < s.length(); i++) {
            if (current == -1 || edgeChildren(edges[current]) == 0)
                return -1;
            current = findEdgeForChar(edgeChildren(edges[current]), s.charAt(i));
        }
        return current;
    }

    public String edgeToString(int edge) {
        return "[" + edgeLetter(edge) + ":" + edgeLastEdge(edge) + ":"
        + edgeAccept(edge) + ":" + edgeChildren(edge) + "]";
    }

    /* These methods extract info from the bits of an edge: */
    private char edgeLetter(int edge) {
        return OrdToChar(edge % 32);
    }

    private boolean edgeLastEdge(int edge) {
        return (edge & 0x00000020) != 0L;
    }

    private boolean edgeAccept(int edge) {
        return (edge & 0x00000040) != 0L;
    }

    private int edgeChildren(int edge) {
        return (int) (edge / 256);
    }

    /* convert an ascii char to an index 1..26. */
    private int CharToOrd(char ch) {
        return Character.toLowerCase(ch) - 'a' + 1;
    }

    /* convert an index to an ascii char a..z. */
    private char OrdToChar(int ord) {
        return ((char) (ord - 1 + 'a'));
    }

    public static void main(String s[]) {
        Lexicon l = new Lexicon(s[0]);
        System.out.println(l.contains("cow"));
        System.out.println(l.contains("cowcow"));
        System.out.println(l.contains("mutton"));
        System.out.println(l.contains("a"));
        System.out.println(l.contains("at"));
        System.out.println(l.contains("i"));
        System.out.println(l.contains("me"));
        System.out.println(l.containsPrefix("i"));
        System.out.println(l.containsPrefix("id"));
        System.out.println(l.containsPrefix("idd"));
    }

}

