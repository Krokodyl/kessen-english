package compression.tree;

import compression.CompressedByte;
import compression.DataByte;
import compression.RepeatByte;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Node {

    int offset;
    int offsetRepeat = -1;
    int repeatLength = 0;
    String data;
    Node repeatOf;
    NodeType type = NodeType.UNTREATED;

    Node left;
    Node right;
    Node parent;

    public Node(String data, Node parent) {
        this.data = data;
        this.parent = parent;
    }

    public List<CompressedByte> getCompressedByte() {
        List<CompressedByte> cbs = new ArrayList<>();
        if (left!=null) cbs.addAll(left.getCompressedByte());
        if (type==NodeType.DATA) cbs.add(new DataByte(Integer.parseInt(data, 16) & 0xFF));
        else if ((type==NodeType.REPEAT && offsetRepeat==-1) || type==NodeType.UNTREATED) {
            for (int i=0;i<data.length();i=i+2) {
                cbs.add(new DataByte(Integer.parseInt(data.substring(i, i+2), 16) & 0xFF));
            }
        } else if (type==NodeType.REPEAT) {
            RepeatByte rb = new RepeatByte(repeatLength-2, offset/2 - offsetRepeat/2 -1);
            cbs.add(rb);
        }
        if (right!=null) cbs.addAll(right.getCompressedByte());
        return cbs;
    }

    public void println() {
        if (left!=null) left.println();
        System.out.println(offset+" "+data+" "+type+" "+getRepeatLength()+"   "+offsetRepeat);
        if (right!=null) right.println();
    }

    public void split(String pattern) {
        if (offset==929) {
            System.out.println();
        }
        int index = data.indexOf(pattern);
        while (index>=0 && index%2!=0) index = data.indexOf(pattern, index + 1);
        if (index>=0) {
            String leftData = data.substring(0, index);
            String rightData = data.substring(index+pattern.length());
            setType(NodeType.REPEAT);
            setRepeatLength(pattern.length()/2);
            if (!leftData.isEmpty()) {
                left = new Node(leftData, this);
            }
            if (!rightData.isEmpty()) {
                right = new Node(rightData, this);
            }
            data = pattern;
            if (left != null) {
                left.setOffset(offset);
                left.split(pattern);
            }
            offset += index;
            if (right!=null) {
                right.setOffset(offset+pattern.length());
                right.split(pattern);
            }
        } else {
            if (left!=null) left.split(pattern);
            if (right!=null) right.split(pattern);
        }
    }

    public List<Node> getUntreatedNodes() {
        List<Node> nodes = new ArrayList<>();
        if (type==NodeType.UNTREATED) nodes.add(this);
        if (left!=null) nodes.addAll(left.getUntreatedNodes());
        if (right!=null) nodes.addAll(right.getUntreatedNodes());
        return nodes;
    }

    public int countType(NodeType nt) {
        int i = 0;
        if (type==nt) i = 1;
        return i + (left!=null?left.countType(nt):0) + (right!=null? right.countType(nt) : 0);
    }

    public int getTreeSize(NodeType nt) {
        int size = getSize();
        if (type!=nt) size = 0;
        return size + (left!=null?left.getTreeSize(nt):0) + (right!=null?right.getTreeSize(nt):0);
    }

    public Map<String, Integer> updateRepeat(Map<String, Integer> patternOffset) {

        if (left!=null) patternOffset.putAll(left.updateRepeat(patternOffset));
        if (type==NodeType.REPEAT) {
            Integer found = patternOffset.get(data);
            if (found==null) {
                patternOffset.put(data, offset);
                if (data.matches("^(.)\\1*$")) {
                    compressRepeatingData();
                }
            } else {
                offsetRepeat = found.intValue();
                patternOffset.put(data, offset);
            }
        }
        if (right!=null) patternOffset.putAll(right.updateRepeat(patternOffset));
        return patternOffset;
    }

    private void compressRepeatingData() {
        Node dataNode = new Node(data.substring(0,2), this);
        dataNode.setType(NodeType.DATA);
        if (left!=null) {
            left.setParent(dataNode);
            dataNode.setLeft(left);
        }
        left = dataNode;
        dataNode.setOffset(offset);
        setOffset(offset+2);
        setRepeatOf(dataNode);
        setType(NodeType.REPEAT);
        setOffsetRepeat(dataNode.getOffset());
        setRepeatLength(data.length()/2-1);
    }

    public int getSize() {
        int size = data.length()/2;
        if (type==NodeType.REPEAT) size = 2;
        if (type==NodeType.DATA) size = 1;
        return size;
    }

    public int getTreeSize() {
        int size = data.length()/2;
        if (type==NodeType.REPEAT) size = 2;
        if (type==NodeType.DATA) size = 1;
        return size + (left!=null?left.getTreeSize():0) + (right!=null?right.getTreeSize():0);
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public Node getLeft() {
        return left;
    }

    public void setLeft(Node left) {
        this.left = left;
    }

    public Node getRight() {
        return right;
    }

    public void setRight(Node right) {
        this.right = right;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public Node getRepeatOf() {
        return repeatOf;
    }

    public void setRepeatOf(Node repeatOf) {
        this.repeatOf = repeatOf;
    }

    public NodeType getType() {
        return type;
    }

    public void setType(NodeType type) {
        this.type = type;
    }

    public String getTreeData() {
        return (left!=null? left.getTreeData():"")+getData()+(right!=null?right.getTreeData():"");
    }

    public Node getParent() {
        return parent;
    }

    public void setParent(Node parent) {
        this.parent = parent;
    }

    public int getOffsetRepeat() {
        return offsetRepeat;
    }

    public void setOffsetRepeat(int offsetRepeat) {
        this.offsetRepeat = offsetRepeat;
    }

    public int getRepeatLength() {
        return repeatLength;
    }

    public void setRepeatLength(int repeatLength) {
        this.repeatLength = repeatLength;
    }

    @Override
    public String toString() {
        return "Node{" +
                "offset=" + offset +
                ", offsetRepeat=" + offsetRepeat +
                ", data='" + data + '\'' +
                ", repeatOf=" + repeatOf +
                ", type=" + type +
                ", left=" + left +
                ", right=" + right +
                '}';
    }


}
