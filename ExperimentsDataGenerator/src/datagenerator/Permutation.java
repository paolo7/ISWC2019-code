package datagenerator;public class Permutation
{
	int[]    m_a;   
	int[]    m_d;   
	int[]    m_e;   
	int      m_size;   

	/////////////////////////////////////////////////////////   
	/** constructs the initial permutation of integers from 0, 1, 2,,,size   
	 */   
	public Permutation(int size) {   
		m_a=new int[size];   
		m_d=new int[size];   
		m_e=new int[size];   

		m_a[0]=0;   
		for(int i=1; i<size; ++i) {   
			m_a[i]=i;   
			m_d[i]=i;   
			m_e[i]=-1;   
		}   
		m_size=size;   
	}   
	
	/** generates the next permutation. If the function returns n, then the  
	 elements at position n and n+1 in the previous permutation were  
	 interchanged to get the new permutation.   
	 @return the index of the lower element which was interchanged  
	 or -1 if the last permutation has been reached.   
	 */   
	public int getNext() {   
		int v=0;   
		for(int k=m_size-1; k>=1; --k) {   
			int q;   
			q=m_d[k]+m_e[k];   
			m_d[k]=q;   
			if(q==k)   
				m_e[k]=-1;   
			else if(q==-1) {   
				m_e[k]=1;   
				++v;   
			}   
			else {   
				q+=v;   
				int x=m_a[q];   
				m_a[q]=m_a[q+1];   
				m_a[q+1]=x;   
				
				return q;   
			}   
		}   
		
		return -1;   
	}   
	
	/** get the current permutation */   
	public int[] getCurrent() {   
		return m_a;   
	}   

}
