package alifeSim.Scenario;

public class ScenarioKeyValuePair<K,V>
{
	private final K key;
	private final V value;	
	
	public ScenarioKeyValuePair(K key,V value)
	{
		this.key = key;
		this.value = value;
	}

	public K getKey()
	{
		return key;
	}

	public V getValue()
	{
		return value;
	}
	
}
