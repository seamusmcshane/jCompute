package jcompute.datastruct.heap;

import jcompute.datastruct.heap.BinaryHeap.HeapType;
import jcompute.timing.TimerObj;
import jcompute.util.text.JCText;

public class HeapTest
{
	public static void main(String args[])
	{
		 test();
		
		 Bench(HeapType.MIN);
		 Bench(HeapType.MAX);
	}
	
	public static void Bench(HeapType type)
	{
		BinaryHeap<Object> heap = new BinaryHeap<Object>(type, 64);
		
		TimerObj timer = new TimerObj();
		
		for(int i = 0; i < 10000; i++)
		{
			heap.push(i, i);
		}
		
		heap = new BinaryHeap<Object>(HeapType.MIN, 64);
		
		timer.startTimer();
		
		for(int i = 0; i < 10000000; i++)
		{
			heap.push(i, i);
		}
		
		for(int i = 0; i < 10000000; i++)
		{
			heap.poll();
		}
		
		timer.stopTimer();
		
		System.out.println("Bench " + timer.getTimeTaken());
		System.out.println("heap " + heap.contents());
	}
	
	public static void test()
	{
		BinaryHeap<Object> minHeap = new BinaryHeap<Object>(HeapType.MIN, 5);
		BinaryHeap<Object> maxHeap = new BinaryHeap<Object>(HeapType.MAX, 5);
		
		System.out.println(JCText.CharRepeatBounded('-', 80));
		System.out.println("Min Heap");
		System.out.println(JCText.CharRepeatBounded('-', 80));
		testHeap(minHeap);
		
		System.out.println(JCText.CharRepeatBounded('-', 80));
		System.out.println("Max Heap");
		System.out.println(JCText.CharRepeatBounded('-', 80));
		testHeap(maxHeap);
	}
	
	public static void testHeap(BinaryHeap<Object> heap)
	{
		addHeap(10, heap);
		addHeap(-10, heap);
		addHeap(-20, heap);
		addHeap(1000, heap);
		
		System.out.println("poll " + heap.poll());
		System.out.println("poll " + heap.poll());
		System.out.println("poll " + heap.poll());
		System.out.println("poll " + heap.poll());
		
		System.out.println("Final");
		heap.dumpHeapArray();
	}
	
	public static void addHeap(int val, BinaryHeap<Object> heap)
	{
		System.out.println("ADD " + val);
		heap.push(val, val);
		heap.dumpHeapArray();
	}
}
