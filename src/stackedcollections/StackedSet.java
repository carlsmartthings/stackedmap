package stackedcollections;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.LinkedBlockingDeque;

public class StackedSet<E> extends AbstractSet<E> implements Set<E>
{
  protected Set<E> topSet = null;

  protected LinkedBlockingDeque<Set<E>> deque = new LinkedBlockingDeque<>();

  protected Map<String, Set<E>> scopeIndex = null;

  // ---- constructors

  public StackedSet()
  {
  }

  public StackedSet(Collection<? extends E> c)
  {
    if (c instanceof Set)
    {
      this.push((Set) c);
    }
    else
    {
      HashSet<E> newset = new HashSet<>();
      newset.addAll(c);
      this.push(newset);
    }
  }

  public StackedSet(Set<? extends E> c, String scope)
  {
    this.push((Set) c, scope);
  }

  // ---- nonstandard Set methods

  public Set<E> pop()
  {
    if (topSet == null)
    {
      throw new IllegalArgumentException("Cannot pop stacked set, there are no more sets to pop.");
    }
    synchronized (deque)
    {
      Set<E> oldTopSet = topSet;
      deque.removeFirst();
      try
      {
        topSet = deque.getFirst();
      }
      catch (NoSuchElementException ex)
      {
        topSet = null;
      }
      return oldTopSet;
    }
  }

  public Set<E> top()
  {
    return topSet;
  }

  public void push(Set<E> set)
  {
    if (set == null)
    {
      throw new IllegalArgumentException("Cannot push null into stacked set");
    }
    // TODO: improve
    synchronized (deque)
    {
      deque.addFirst(set);
      topSet = set;
    }
  }

  public void push(Set<E> set, String scopeName)
  {
    if (scopeName == null)
    {
      throw new IllegalArgumentException("push of scoped set requires a non-null scope name");
    }
    // TODO: ?improve?
    synchronized (deque)
    {
      if (scopeIndex == null)
      {
        scopeIndex = new HashMap<>();
        deque.addFirst(set);
        topSet = set;
        scopeIndex.put(scopeName, set);
      }
    }

  }

  // TODO: use less exceptions and just return nulls?
  public Set<E> getScope(String scopeName)
  {
    if (scopeIndex == null)
    {
      throw new IllegalStateException("No named scopes have been specified for stacked set");
    }
    if (scopeIndex.containsKey(scopeName))
    {
      return scopeIndex.get(scopeName);
    }
    throw new IllegalArgumentException("Scope " + scopeName + " not specified in stacked set");
  }

  public Map<String, Set<E>> getScopes()
  {
    if (scopeIndex == null)
    {
      throw new IllegalArgumentException("no named scopes specified for stacked set");
    }
    return scopeIndex;
  }

  // ---- java.util.Set methods

  /**
   * Size is expensive currently, as we need to compute the overall set of elements from the constituent sets.
   * Caching as we add sets is a bad idea if the constituent sets in the stack change... the nature of these is basically
   * 
   * @return
   */
  @Override
  public int size()
  {
    if (topSet == null)
    {
      return 0;
    }
    Iterator<E> iter = this.iterator();
    int count = 0;
    while (iter.hasNext())
    {
      count++;
      iter.next();
    }
    return count;
  }

  @Override
  public boolean isEmpty()
  {
    if (topSet == null)
    {
      return true;
    }
    Iterator<Set<E>> iter = deque.iterator();
    while (iter.hasNext())
    {
      if (!iter.next().isEmpty())
      {
        return false;
      }
    }
    return true;
  }

  @Override
  public boolean contains(Object o)
  {
    if (topSet == null)
    {
      return false;
    }
    Iterator<Set<E>> iter = deque.iterator();
    while (iter.hasNext())
    {
      Set<E> next = iter.next();
      if (next.contains(o))
      {
        return true;
      }
    }
    return false;
  }

  @Override
  public Iterator<E> iterator()
  {
    if (topSet == null)
    {
      return Collections.EMPTY_SET.iterator();
    }
    return new StackedSetIterator<E>(deque);
  }

  @Override
  public boolean add(E e)
  {
    if (topSet == null)
    {
      throw new IllegalArgumentException("At least one map must be pushed into the stacked map");
    }
    return topSet.add(e);
  }

  @Override
  public boolean remove(Object o)
  {
    if (topSet == null)
    {
      throw new IllegalArgumentException("At least one map must be pushed into the stacked map");
    }
    return topSet.remove(o);
  }

  @Override
  public boolean addAll(Collection<? extends E> c)
  {
    if (topSet == null)
    {
      throw new IllegalArgumentException("At least one map must be pushed into the stacked map");
    }
    return topSet.addAll(c);
  }

  @Override
  public boolean retainAll(Collection<?> c)
  {
    return topSet.retainAll(c);
  }

  @Override
  public boolean removeAll(Collection<?> c)
  {
    return topSet.removeAll(c);
  }

  @Override
  public void clear()
  {
    topSet.clear();
  }

}

// TODO: I think we need a tracking set to make sure we don't duplicate elements at different scope levels...
// .... OOOF, this is a tough problem with hasNext() and lookahead...
class StackedSetIterator<E> implements Iterator<E>
{
  StackedSetIterator(LinkedBlockingDeque<Set<E>> dequeIN)
  {
    dequeIterator = dequeIN.iterator();
    curIterator = dequeIterator.next().iterator();
  }

  Iterator<Set<E>> dequeIterator = null;
  Iterator<E> curIterator = null;
  boolean lookahead = false;
  E lookaheadElement = null;
  Set<E> encounteredElements = new HashSet<>();

  @Override
  public boolean hasNext()
  {
    if (lookahead)
    {
      return true;
    }
    // LOOKAHEAD... ugh
    while (true)
    {
      if (curIterator.hasNext())
      {
        lookahead = false;
        lookaheadElement = curIterator.next();
        if (!encounteredElements.contains(lookaheadElement))
        {
          lookahead = true;
          return true;
        }
      }
      else
      {
        // get next curIterator
        if (dequeIterator.hasNext())
        {
          curIterator = dequeIterator.next().iterator();
        }
        else
        {
          // all out...
          lookahead = false;
          return false;
        }
      }
    }
  }

  @Override
  public E next()
  {
    if (lookahead)
    {
      encounteredElements.add(lookaheadElement);
      lookahead = false;
      return lookaheadElement;
    }
    if (curIterator.hasNext())
    {
      return curIterator.next();
    }
    while (true)
    {
      if (!dequeIterator.hasNext())
      {
        return null;
      }
      curIterator = dequeIterator.next().iterator();
      if (curIterator.hasNext())
      {
        return curIterator.next();
      }
    }
  }

}
