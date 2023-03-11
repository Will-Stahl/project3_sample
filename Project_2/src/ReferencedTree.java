import java.util.*;

/**
 * tree where each node contains an index, a value, and a list of child nodes
 * an ancillary array uses these indices to reference each node directly
 * updates are quick and it can be iterated recursively or by order of creation
 */
public class ReferencedTree {
    private class ReferencedNode {
        public int ID;
        public String article;
        public ArrayList<ReferencedNode> children;

        public ReferencedNode(int ID, String article) {
            this.ID = ID;
            this.article = article;
        }
    }
    private ReferencedNode root;
    private ArrayList<ReferencedNode> directList;

    public ReferencedTree() {
        root.ID = 0;
        root.article = "";
        root.children = new ArrayList<ReferencedNode>();
        directList.add(root);
    }

    /**
     * @param article new article to publish
     * returns false if replyTo doesn't exist
     * @param newID unique ID generated by coordinator
     * false if newID != the new index added to directList
     * @param replyTo ID of article to reply to, highest level if 0
     */
    public boolean AddNode(int newID, String article, int replyTo) {
        // TODO: use replyTo to index directList
        ReferencedNode replyToNode = directList.get(replyTo);
        if (replyToNode == null) {
            return false;
        }
        if (directList.size() != newID) {
            return false;  // tried to add wrong unique ID
        }
        ReferencedNode toAdd = new ReferencedNode(newID, article);
        toAdd.children = new ArrayList<ReferencedNode>();
        replyToNode.children.add(toAdd);
        directList.add(toAdd);
        return true;
    }

    /**
     * returns preview of all articles with indentation and IDs
     */
    public String ReadTree() {
        // TODO: recursively construct preview string like in the writeup
        // do not include the root in the string
        String result = "";
        for (ReferencedNode child : root.children) {
            result += ReadChild(child, "");
        }
        return result;
    }

    /**
     * recursive helper for Read()
     */
    private String ReadChild(ReferencedNode parent, String indent) {
        String result = "\n" + indent + parent.ID + " ";
        if (parent.article.length() > 16) {  // too long, cut to preview
            result += parent.article.substring(0, 12) + "...";
        }
        for (ReferencedNode child : root.children) {
            result += ReadChild(child, indent + "  ");
        }
        return result;
    }

    public String GetAtIndex(int idx) {
        ReferencedNode node = directList.get(idx);
        if (node == null) {
            return null;
        }
        return node.article;
    }

}