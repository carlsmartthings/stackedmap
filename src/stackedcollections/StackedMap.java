package stackedcollections;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.LinkedBlockingDeque;

public class StackedMap<K, V> extends AbstractMap<K, V> implements Map<K, V>
{
  protected Map<K, V> topMap = null;
  // TODO: protected String topScopeName = null
  protected LinkedBlockingDeque<Map<K, V>> deque = new LinkedBlockingDeque<>();
  protected Map<String, Map<K, V>> scopeIndex = null;

  // ---- constructors
  public StackedMap()
  {
  }

  public StackedMap(Map<? extends K, ? extends V> m)
  {
    push((Map) m);
  }

  public StackedMap(Map<? extends K, ? extends V> m, String scopeName)
  {
    push((Map) m, scopeName);
  }

  // ---- stacked map special methods

  public Map<K, V> pop()
  {
    if (topMap == null)
    {
      throw new IllegalArgumentException("Cannot pop stacked map, there are no more mapa to pop.");
    }
    synchronized (deque)
    {
      Map<K, V> oldTopMap = topMap;
      deque.removeFirst();
      try
      {
        topMap = deque.getFirst();
      }
      catch (NoSuchElementException ex)
      {
        topMap = null;
      }
      return oldTopMap;
    }
  }

  public Map<K, V> top()
  {
    return topMap;
  }

  public void push(Map<K, V> map)
  {
    if (map == null)
    {
      throw new IllegalArgumentException("Cannot push null into stacked map");
    }
    // TODO: improve thread access?
    synchronized (deque)
    {
      deque.addFirst(map);
      topMap = map;
    }
  }

  public void push(Map<K, V> map, String scopeName)
  {
    if (scopeName == null)
    {
      throw new IllegalArgumentException("push of scoped map requires a non-null scope name");
    }
    // TODO: ?improve?
    synchronized (deque)
    {
      if (scopeIndex == null)
      {
        scopeIndex = new HashMap<>();
      }
      deque.addFirst(map);
      topMap = map;
      scopeIndex.put(scopeName, map);
    }

  }

  public void clearStack()
  {
    topMap = null;
    deque.clear();
  }

  public Map<String, Map<K, V>> getScopes()
  {
    if (scopeIndex == null)
    {
      throw new IllegalArgumentException("no named scopes specified for stacked map");
    }
    return scopeIndex;
  }

  // TODO: use less exceptions and just return nulls?
  public Map<K, V> getScope(String scopeName)
  {
    if (scopeIndex == null)
    {
      throw new IllegalStateException("No named scopes have been specified for stacked map");
    }
    if (scopeIndex.containsKey(scopeName))
    {
      return scopeIndex.get(scopeName);
    }
    throw new IllegalArgumentException("Scope " + scopeName + " not specified in stacked map");
  }

  // ---- java.util.Map methods

  @Override
  public V get(Object key)
  {
    if (topMap == null)
    {
      return null;
    }
    Iterator<Map<K, V>> iter = deque.iterator();
    while (iter.hasNext())
    {
      Map<K, V> map = iter.next();
      if (map.containsKey(key))
      {
        return map.get(key);
      }
    }
    return null;
  }

  @Override
  public boolean containsKey(Object key)
  {
    if (topMap == null)
    {
      return false;
    }
    Iterator<Map<K, V>> iter = deque.iterator();
    while (iter.hasNext())
    {
      Map<K, V> map = iter.next();
      if (map.containsKey(key))
      {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean containsValue(Object value)
  {
    if (topMap == null)
    {
      return false;
    }
    Iterator<Map<K, V>> iter = deque.iterator();
    while (iter.hasNext())
    {
      Map<K, V> map = iter.next();
      if (map.containsValue(value))
      {
        return true;
      }
    }
    return false;
  }

  @Override
  public Collection<V> values()
  {
    List<V> vals = new ArrayList<>();
    Iterator<Map<K, V>> i = deque.iterator();
    while (i.hasNext())
    {
      Map<K, V> m = i.next();
      vals.addAll(m.values());
    }
    return vals;

  }

  @Override
  public int size()
  {
    if (topMap == null)
    {
      return 0;
    }
    return keySet().size();
  }

  @Override
  public Set<K> keySet()
  {
    StackedSet<K> ss = new StackedSet<>();
    Iterator<Map<K, V>> i = deque.descendingIterator();
    while (i.hasNext())
    {
      ss.push(i.next().keySet());
    }
    return ss;
  }

  @Override
  public Set<Entry<K, V>> entrySet()
  {
    StackedSet<Entry<K, V>> ss = new StackedSet<>();
    Iterator<Map<K, V>> i = deque.descendingIterator();
    while (i.hasNext())
    {
      ss.push(i.next().entrySet());
    }
    return ss;
  }

  @Override
  public V put(K key, V value)
  {
    if (topMap == null)
    {
      throw new IllegalStateException("stacked map has no maps pushed yet");
    }
    return topMap.put(key, value);
  }

  @Override
  public void putAll(Map<? extends K, ? extends V> m)
  {
    if (topMap == null)
    {
      throw new IllegalStateException("stacked map has no maps pushed yet");
    }
    for (Entry<? extends K, ? extends V> entry : m.entrySet())
    {
      put(entry.getKey(), entry.getValue());
    }
  }

  @Override
  public V remove(Object key)
  {
    if (topMap == null)
    {
      throw new IllegalStateException("stacked map has no maps pushed yet");
    }
    return topMap.remove(key);
  }

  @Override
  public void clear()
  {
    if (topMap == null)
    {
      throw new IllegalStateException("stacked map has no maps pushed yet");
    }
    topMap.clear();
  }
}
